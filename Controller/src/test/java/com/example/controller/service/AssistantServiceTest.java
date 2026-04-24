package com.example.controller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.controller.DTO.AssistantChatRequest;
import com.example.controller.DTO.AssistantChatResponse;
import com.example.controller.DTO.AssistantChatTurn;
import com.example.controller.DTO.ProductDTO;
import com.example.controller.client.CartClient;
import com.example.controller.client.LlmCompletionClient;
import com.example.controller.client.ProductClient;
import com.example.controller.config.AssistantProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private ProductClient productClient;

    @Mock
    private CartClient cartClient;

    @Mock
    private LlmCompletionClient llmCompletionClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AssistantProperties assistantProperties;

    private AssistantService assistantService;

    @BeforeEach
    void setUp() {
        assistantProperties = new AssistantProperties();
        assistantProperties.setModel("test-model");
        assistantProperties.setMaxCatalogItems(100);
        assistantService =
                new AssistantService(productClient, cartClient, llmCompletionClient, objectMapper, assistantProperties);
    }

    @Test
    void extractLegacyBraceIdsFromReply_parsesTags() {
        assertThat(AssistantService.extractLegacyBraceIdsFromReply("{id : 6} - телефон Samsung\n{id:7}-ещё строка"))
                .containsExactly(6L, 7L);
        assertThat(AssistantService.extractLegacyBraceIdsFromReply("нет тегов")).isEmpty();
    }

    @Test
    void extractProductIdsFromBoldCatalogNames_matchesCatalogName() {
        ProductDTO p6 = new ProductDTO();
        p6.setId(6L);
        p6.setName("phone_samsung");
        Map<Long, ProductDTO> byId = new LinkedHashMap<>();
        byId.put(6L, p6);
        assertThat(AssistantService.extractProductIdsFromBoldCatalogNames("**phone_samsung** — классный телефон", byId))
                .containsExactly(6L);
    }

    @Test
    void chat_mentionedIds_derivedFromBold_whenPayloadIdsEmpty() throws Exception {
        ProductDTO p6 = new ProductDTO();
        p6.setId(6L);
        p6.setName("phone_samsung");
        p6.setDescription("d");
        p6.setCountOfProduct(1);
        p6.setRating(4f);
        p6.setSellerId(1L);
        when(productClient.allProducts()).thenReturn(List.of(p6));

        String llmJson =
                """
                {"userMessage":"**phone_samsung** — отличный телефон","mentionedProductIds":[],"actions":[]}
                """;
        when(llmCompletionClient.complete(eq("test-model"), anyList())).thenReturn(llmJson);

        AssistantChatRequest req = new AssistantChatRequest();
        req.setMessage("посоветуй");

        AssistantChatResponse res = assistantService.chat("Bearer t", req);

        assertThat(res.getReply()).contains("**phone_samsung**");
        assertThat(res.getMentionedProductIds()).containsExactly(6L);
    }

    @Test
    void chat_blankMessage_throws() {
        AssistantChatRequest req = new AssistantChatRequest();
        req.setMessage("   ");
        assertThatThrownBy(() -> assistantService.chat("Bearer t", req)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chat_addToCart_executesWhenProductInCatalog() throws Exception {
        ProductDTO p = new ProductDTO();
        p.setId(7L);
        p.setName("Mug");
        p.setDescription("Ceramic");
        p.setCountOfProduct(3);
        p.setRating(4.2f);
        p.setSellerId(1L);
        when(productClient.allProducts()).thenReturn(List.of(p));

        String llmJson =
                """
                {"userMessage":"Добавил кружку","mentionedProductIds":[7],"actions":[{"type":"ADD_TO_CART","productId":7}]}
                """;
        when(llmCompletionClient.complete(eq("test-model"), anyList())).thenReturn(llmJson);

        AssistantChatRequest req = new AssistantChatRequest();
        req.setMessage("Положи кружку в корзину");

        AssistantChatResponse res = assistantService.chat("Bearer token", req);

        assertThat(res.getReply()).isEqualTo("Добавил кружку");
        assertThat(res.getMentionedProductIds()).containsExactly(7L);
        assertThat(res.getExecutedActions()).containsExactly("ADD_TO_CART:7");

        verify(cartClient, times(1)).addProductToCart(eq("Bearer token"), eq("Mug"));
    }

    @Test
    void chat_addToCart_skipsUnknownProductId() throws Exception {
        ProductDTO p = new ProductDTO();
        p.setId(1L);
        p.setName("A");
        when(productClient.allProducts()).thenReturn(List.of(p));

        String llmJson =
                """
                {"userMessage":"Ок","mentionedProductIds":[],"actions":[{"type":"ADD_TO_CART","productId":999}]}
                """;
        when(llmCompletionClient.complete(eq("test-model"), anyList())).thenReturn(llmJson);

        AssistantChatRequest req = new AssistantChatRequest();
        req.setMessage("x");

        AssistantChatResponse res = assistantService.chat("Bearer t", req);

        assertThat(res.getExecutedActions()).isEmpty();
        verify(cartClient, never()).addProductToCart(anyString(), anyString());
    }

    @Test
    void chat_alwaysLoadsFullCatalog() throws Exception {
        when(productClient.allProducts()).thenReturn(List.of());

        when(llmCompletionClient.complete(eq("test-model"), anyList()))
                .thenReturn("{\"userMessage\":\"Нет товаров\",\"mentionedProductIds\":[],\"actions\":[]}");

        AssistantChatRequest req = new AssistantChatRequest();
        req.setMessage("?");

        assistantService.chat("Bearer t", req);

        verify(productClient).allProducts();
        verify(productClient, never()).findProductsByWord(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void chat_passesHistoryToLlm() throws Exception {
        when(productClient.allProducts()).thenReturn(List.of());
        when(llmCompletionClient.complete(eq("test-model"), anyList()))
                .thenReturn("{\"userMessage\":\"ok\",\"mentionedProductIds\":[],\"actions\":[]}");

        AssistantChatRequest req = new AssistantChatRequest();
        req.setMessage("hi");
        req.setHistory(List.of(
                new AssistantChatTurn("user", "prev user"), new AssistantChatTurn("assistant", "prev assistant")));

        assistantService.chat("Bearer t", req);

        ArgumentCaptor<List<Map<String, String>>> cap = ArgumentCaptor.forClass(List.class);
        verify(llmCompletionClient).complete(eq("test-model"), cap.capture());
        List<Map<String, String>> msgs = cap.getValue();
        assertThat(msgs.get(0).get("role")).isEqualTo("system");
        assertThat(msgs.get(1).get("role")).isEqualTo("user");
        assertThat(msgs.get(1).get("content")).isEqualTo("prev user");
        assertThat(msgs.get(2).get("role")).isEqualTo("assistant");
        assertThat(msgs.get(3).get("role")).isEqualTo("user");
        assertThat(msgs.get(3).get("content")).isEqualTo("hi");
    }
}
