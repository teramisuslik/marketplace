package com.example.controller.service;

import com.example.controller.DTO.AssistantChatRequest;
import com.example.controller.DTO.AssistantChatResponse;
import com.example.controller.DTO.AssistantChatTurn;
import com.example.controller.DTO.LlmAssistantAction;
import com.example.controller.DTO.LlmAssistantPayload;
import com.example.controller.DTO.ProductDTO;
import com.example.controller.client.CartClient;
import com.example.controller.client.LlmCompletionClient;
import com.example.controller.client.ProductClient;
import com.example.controller.config.AssistantProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssistantService {

    private static final int MAX_HISTORY_TURNS = 10;

    /** Рекомендации: внутри **…** — в начале точное значение поля {@code name} из каталога. */
    private static final Pattern BOLD_CATALOG_SEGMENT = Pattern.compile("\\*\\*(.+?)\\*\\*", Pattern.DOTALL);

    /** Устаревший формат `{id: 6}` — подстраховка, если модель всё ещё так пишет. */
    private static final Pattern LEGACY_LINE_ID =
            Pattern.compile("\\{id\\s*:\\s*(\\d+)\\s*}", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private final ProductClient productClient;
    private final CartClient cartClient;
    private final LlmCompletionClient llmCompletionClient;
    private final ObjectMapper objectMapper;
    private final AssistantProperties assistantProperties;

    public AssistantChatResponse chat(String authorizationHeader, AssistantChatRequest request) {
        if (!StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("message is required");
        }

        List<ProductDTO> catalog = loadCatalog();
        int loadedCount = catalog.size();
        int max = Math.max(1, assistantProperties.getMaxCatalogItems());
        if (catalog.size() > max) {
            log.info(
                    "Ассистент: в каталоге {} товаров, в промпт передаём первые {} (max-catalog-items)",
                    loadedCount,
                    max);
            catalog = catalog.subList(0, max);
        } else {
            log.info("Ассистент: в каталоге {} товаров для контекста", loadedCount);
        }

        Map<Long, ProductDTO> byId = new LinkedHashMap<>();
        for (ProductDTO p : catalog) {
            if (p.getId() != null) {
                byId.put(p.getId(), p);
            }
        }

        String catalogJson;
        try {
            catalogJson = objectMapper.writeValueAsString(catalog);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize catalog", e);
        }

        String systemContent =
                """
                You are a marketplace shopping assistant.

                Always return exactly ONE JSON object (no markdown code fences around it):
                {"userMessage":"...","mentionedProductIds":[],"actions":[]}

                userMessage:
                - Natural Russian only for what the customer reads. Friendly, clear. Do not mix English words.
                - Never print numeric product ids inside userMessage (ids belong only in mentionedProductIds).

                Recommendations or comparisons (e.g. "посоветуй", "что выбрать", "какой телефон лучше") when the user did NOT ask to add to cart or to buy right now:
                - "actions" must be [] — never ADD_TO_CART on advice-only turns.
                - For each catalog item you recommend, start a segment with markdown bold using the EXACT catalog "name" string from JSON, then optional em dash and your Russian comment, e.g. **phone_iphone_15** — отличная экосистема Apple.
                - Use one such **name** block per recommended product (name must match the JSON field "name" character-for-character).
                - "mentionedProductIds": the numeric "id" values for those same products, in the same order as the ** segments appear.

                Add to cart (user clearly wants basket: "добавь в корзину", "положи в корзину", "оформи покупку", "давай добавим в корзину", etc.):
                - You may set "actions" to [{"type":"ADD_TO_CART","productId":<id from catalog>}]. userMessage can be short Russian; **name** blocks are optional here.

                Small talk (hello, thanks): short Russian; mentionedProductIds []; actions [].

                Only products from the catalog JSON below. Never invent ids or names.

                Catalog products (JSON array):
                """
                        + catalogJson;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemContent));

        int historyTail = appendHistory(messages, request.getHistory());
        if (historyTail > 0) {
            log.info("Ассистент: в запрос к модели добавлено {} реплик из истории", historyTail);
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        String model = assistantProperties.getModel();
        log.info("Ассистент: вызов LLM, модель={}, сообщений в чате={}", model, messages.size());
        String raw;
        try {
            raw = llmCompletionClient.complete(model, messages);
        } catch (Exception e) {
            log.error("Ассистент: ошибка вызова LLM (модель {})", model, e);
            return AssistantChatResponse.builder()
                    .reply("Сервис ассистента временно недоступен. Проверьте, что Ollama запущен и модель загружена.")
                    .build();
        }

        LlmAssistantPayload payload;
        try {
            payload = parsePayload(raw);
        } catch (Exception e) {
            log.warn("Ассистент: не удалось разобрать JSON модели: {}", truncateForLog(raw, 500), e);
            return AssistantChatResponse.builder()
                    .reply("Не удалось разобрать ответ ассистента. Попробуйте переформулировать вопрос.")
                    .build();
        }

        List<String> executed = new ArrayList<>();
        if (payload.getActions() != null && authorizationHeader != null) {
            for (LlmAssistantAction action : payload.getActions()) {
                if (action == null
                        || !"ADD_TO_CART".equalsIgnoreCase(action.getType())
                        || action.getProductId() == null) {
                    continue;
                }
                ProductDTO product = byId.get(action.getProductId());
                if (product == null || !StringUtils.hasText(product.getName())) {
                    log.warn(
                            "Ассистент: действие ADD_TO_CART пропущено — productId={} нет в переданном каталоге",
                            action.getProductId());
                    continue;
                }
                try {
                    cartClient.addProductToCart(authorizationHeader, product.getName());
                    executed.add("ADD_TO_CART:" + action.getProductId());
                    log.info(
                            "Ассистент: в корзину добавлен товар id={}, name={}",
                            action.getProductId(),
                            product.getName());
                } catch (Exception e) {
                    log.warn(
                            "Ассистент: не удалось добавить в корзину productId={}, name={}",
                            action.getProductId(),
                            product.getName(),
                            e);
                }
            }
        }

        String reply = StringUtils.hasText(payload.getUserMessage()) ? payload.getUserMessage() : "Готово.";
        List<Long> mentioned =
                resolveMentionedProductIds(payload.getUserMessage(), payload.getMentionedProductIds(), byId);

        log.info(
                "Ассистент: ответ сформирован, упомянутых id={}, выполненных действий={}",
                mentioned.size(),
                executed.size());
        return AssistantChatResponse.builder()
                .reply(reply)
                .mentionedProductIds(mentioned)
                .executedActions(executed)
                .build();
    }

    private List<ProductDTO> loadCatalog() {
        log.info("Ассистент: загрузка полного каталога (main)");
        return productClient.allProducts();
    }

    /** @return сколько реплик истории реально добавлено в messages */
    private int appendHistory(List<Map<String, String>> messages, List<AssistantChatTurn> history) {
        if (history == null || history.isEmpty()) {
            return 0;
        }
        List<AssistantChatTurn> tail = history.size() > MAX_HISTORY_TURNS * 2
                ? history.subList(history.size() - MAX_HISTORY_TURNS * 2, history.size())
                : history;
        int added = 0;
        for (AssistantChatTurn turn : tail) {
            if (turn == null || !StringUtils.hasText(turn.getRole()) || !StringUtils.hasText(turn.getContent())) {
                continue;
            }
            String role = turn.getRole().toLowerCase();
            if (!role.equals("user") && !role.equals("assistant")) {
                continue;
            }
            messages.add(Map.of("role", role, "content", turn.getContent()));
            added++;
        }
        return added;
    }

    private LlmAssistantPayload parsePayload(String raw) throws JsonProcessingException {
        String cleaned = stripJsonFence(raw);
        return objectMapper.readValue(cleaned, LlmAssistantPayload.class);
    }

    private static String truncateForLog(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "...";
    }

    private List<Long> resolveMentionedProductIds(
            String userMessage, List<Long> payloadMentioned, Map<Long, ProductDTO> byId) {
        List<Long> fromBold = extractProductIdsFromBoldCatalogNames(userMessage, byId);
        if (!fromBold.isEmpty()) {
            return fromBold;
        }
        List<Long> fromLegacy = extractLegacyBraceIdsFromReply(userMessage);
        List<Long> legacyFiltered =
                fromLegacy.stream().filter(byId::containsKey).collect(Collectors.toCollection(ArrayList::new));
        List<Long> legacyDeduped = new ArrayList<>(new LinkedHashSet<>(legacyFiltered));
        if (!legacyDeduped.isEmpty()) {
            return legacyDeduped;
        }
        if (payloadMentioned == null || payloadMentioned.isEmpty()) {
            return List.of();
        }
        return payloadMentioned.stream().filter(byId::containsKey).collect(Collectors.toList());
    }

    /**
     * Сопоставляет сегменты {@code **slug**} с полем {@code name} товара в каталоге (без показа id в
     * тексте пользователю).
     */
    static List<Long> extractProductIdsFromBoldCatalogNames(String userMessage, Map<Long, ProductDTO> byId) {
        if (!StringUtils.hasText(userMessage) || byId.isEmpty()) {
            return List.of();
        }
        Map<String, Long> nameToId = new HashMap<>();
        for (ProductDTO p : byId.values()) {
            if (p.getName() != null && p.getId() != null) {
                nameToId.put(p.getName().toLowerCase(Locale.ROOT), p.getId());
            }
        }
        List<Long> out = new ArrayList<>();
        Matcher m = BOLD_CATALOG_SEGMENT.matcher(userMessage);
        while (m.find()) {
            String inner = m.group(1).trim();
            if (!StringUtils.hasText(inner)) {
                continue;
            }
            String slug = inner.split("\\s*[—–-]\\s*", 2)[0].trim();
            Long id = nameToId.get(slug.toLowerCase(Locale.ROOT));
            if (id != null) {
                out.add(id);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    /** Устаревший формат {@code {id: 6}} в тексте — порядок появления. */
    static List<Long> extractLegacyBraceIdsFromReply(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            return List.of();
        }
        Matcher m = LEGACY_LINE_ID.matcher(userMessage);
        List<Long> out = new ArrayList<>();
        while (m.find()) {
            try {
                out.add(Long.parseLong(m.group(1)));
            } catch (NumberFormatException ignored) {
                // пропускаем битый фрагмент
            }
        }
        return out;
    }

    static String stripJsonFence(String content) {
        String s = content == null ? "" : content.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            int end = s.lastIndexOf("```");
            if (nl > 0 && end > nl) {
                s = s.substring(nl + 1, end).trim();
            }
        }
        return s;
    }
}
