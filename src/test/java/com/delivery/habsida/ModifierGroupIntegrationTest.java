package com.delivery.habsida;

import com.delivery.habsida.entity.ModifierGroup;
import com.delivery.habsida.entity.Product;
import com.delivery.habsida.entity.ProductModifierGroup;
import com.delivery.habsida.entity.Store;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class ModifierGroupIntegrationTest extends AbstractIntegrationTest {
    private ModifierGroup createModifierGroup(Store store, String name) {
        ModifierGroup modifierGroup = new ModifierGroup();
        modifierGroup.setName(name);
        modifierGroup.setStore(store);
        modifierGroup.setRequired(false);
        modifierGroup.setMinSelect(0);
        modifierGroup.setMaxSelect(3);
        modifierGroup.setModifierOptions(new ArrayList<>());

        return modifierGroupRepository.save(modifierGroup);
    }

    private ProductModifierGroup linkGroupToProduct(ModifierGroup modifierGroup, Product product) {
        ProductModifierGroup productModifierGroup = new ProductModifierGroup();
        productModifierGroup.setModifierGroup(modifierGroup);
        productModifierGroup.setProduct(product);

        return productModifierGroupRepository.save(productModifierGroup);
    }

    @Test
    void getModifierGroups_shouldReturnSortedByNameAsc_whenUserIsAdmin() throws Exception {
        createUser("boss@boss", "ADMIN");
        String token = loginAndGetToken("boss@boss");

        Store store1 = createStore("group-asc-store");

        Product product = createProduct(store1, "HamburgerArtyom", "10.00");

        ModifierGroup sizeGroup = createModifierGroup(store1, "Size");
        ModifierGroup sauceGroup = createModifierGroup(store1, "Sauce");

        linkGroupToProduct(sizeGroup, product);
        linkGroupToProduct(sauceGroup, product);

        String body = mockMvc.perform(get(
                        "/stores/{storeId}/products/{productId}/modifier-groups",
                        store1.getId(), product.getId())
                        .param("sort", "name,asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> listGroups = JsonPath.read(body, "$.content[*].name");
        assertEquals("Sauce", listGroups.get(0));
        assertEquals("Size", listGroups.get(1));
    }

    @Test
    void getModifierGroups_shouldReturnSortedByNameDesc_whenUserIsAdmin() throws Exception {
        createUser("roman@roman", "ADMIN");
        String token = loginAndGetToken("roman@roman");

        Store chelseaStore = createStore("chelsea-blue");

        Product pizzaChelsea = createProduct(chelseaStore, "PizzaChelsea", "10.00");

        ModifierGroup sauceGroup = createModifierGroup(chelseaStore, "Sauce");
        ModifierGroup sizeGroup = createModifierGroup(chelseaStore, "Size");

        linkGroupToProduct(sauceGroup, pizzaChelsea);
        linkGroupToProduct(sizeGroup, pizzaChelsea);

        String body = mockMvc.perform(get("/stores/{storeId}/products/{productId}/modifier-groups",
                        chelseaStore.getId(), pizzaChelsea.getId())
                        .param("sort", "name,desc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> listGroups = JsonPath.read(body, "$.content[*].name");
        assertEquals("Size", listGroups.get(0));
        assertEquals("Sauce", listGroups.get(1));
    }

    @Test
    void getModifierGroups_shouldReturnBadRequest_whenSortFieldIsInvalid() throws Exception {
        createUser("trump@trump", "ADMIN");
        String token = loginAndGetToken("trump@trump");

        Store storeTrump = createStore("store-trump");
        Product hotDog = createProduct(storeTrump, "hot-dog", "10.00");

        ModifierGroup sizeGroup = createModifierGroup(storeTrump, "Size");
        ModifierGroup sauceGroup = createModifierGroup(storeTrump, "Sauce");

        linkGroupToProduct(sizeGroup, hotDog);
        linkGroupToProduct(sauceGroup, hotDog);
        mockMvc.perform(get("/stores/{storeId}/products/{productId}/modifier-groups", storeTrump.getId(), hotDog.getId())
                        .param("sort", "foo,asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
