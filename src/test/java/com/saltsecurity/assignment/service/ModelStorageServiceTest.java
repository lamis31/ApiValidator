package com.saltsecurity.assignment.service;

import com.saltsecurity.assignment.model.ApiModel;
import com.saltsecurity.assignment.model.ParamDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ModelStorageServiceTest {

    private ModelStorageService modelStorageService;

    @BeforeEach
    void setUp() {
        modelStorageService = new ModelStorageService();
        modelStorageService.clearStore();
    }

    @Test
    void storeModels_validModels_shouldStoreSuccessfully() {
        // Setup
        ApiModel model1 = new ApiModel("/test1", "GET", null, null, null);
        ApiModel model2 = new ApiModel("/test2", "POST", null, null, null);
        List<ApiModel> models = Arrays.asList(model1, model2);

        // Execute
        modelStorageService.storeModels(models);

        // Verify
        assertEquals(2, modelStorageService.getStoreSize());
        assertTrue(modelStorageService.findModel("GET", "/test1").isPresent());
        assertTrue(modelStorageService.findModel("POST", "/test2").isPresent());
    }

    @Test
    void storeModels_nullModels_shouldHandleGracefully() {
        // Execute
        modelStorageService.storeModels(null);

        // Verify
        assertEquals(0, modelStorageService.getStoreSize());
    }

    @Test
    void storeModels_invalidModels_shouldSkipInvalidOnes() {
        // Setup - model3 is invalid due to null method and path
        ApiModel model1 = new ApiModel("/test1", "GET", null, null, null);
        ApiModel model2 = null;
        ApiModel model3 = new ApiModel(null, null, null, null, null);
        List<ApiModel> models = Arrays.asList(model1, model2, model3);

        // Execute
        modelStorageService.storeModels(models);

        // Verify - only model1 should be stored
        assertEquals(1, modelStorageService.getStoreSize());
        assertTrue(modelStorageService.findModel("GET", "/test1").isPresent());
    }

    @Test
    void storeModels_duplicateKey_shouldOverwrite() {
        // Setup - two models with same method and path but different parameters
        ParamDefinition param1 = new ParamDefinition("param1", Collections.singletonList("string"), true);
        ParamDefinition param2 = new ParamDefinition("param2", Collections.singletonList("int"), false);

        ApiModel model1 = new ApiModel("/test", "GET", 
                Collections.singletonList(param1), null, null);
        
        ApiModel model2 = new ApiModel("/test", "GET", 
                Collections.singletonList(param2), null, null);
        
        // Execute - store model1 then model2
        modelStorageService.storeModels(Collections.singletonList(model1));
        modelStorageService.storeModels(Collections.singletonList(model2));

        // Verify - model2 should have overwritten model1
        assertEquals(1, modelStorageService.getStoreSize());
        Optional<ApiModel> storedModel = modelStorageService.findModel("GET", "/test");
        assertTrue(storedModel.isPresent());
        assertEquals(1, storedModel.get().getQuery_params().size());
        assertEquals("param2", storedModel.get().getQuery_params().get(0).getName());
    }

    @Test
    void findModel_existingModel_shouldReturnModel() {
        // Setup
        ApiModel model = new ApiModel("/test", "GET", null, null, null);
        modelStorageService.storeModels(Collections.singletonList(model));

        // Execute
        Optional<ApiModel> result = modelStorageService.findModel("GET", "/test");

        // Verify
        assertTrue(result.isPresent());
        assertEquals("/test", result.get().getPath());
        assertEquals("GET", result.get().getMethod());
    }

    @Test
    void findModel_nonExistingModel_shouldReturnEmpty() {
        // Execute
        Optional<ApiModel> result = modelStorageService.findModel("GET", "/nonexistent");

        // Verify
        assertFalse(result.isPresent());
    }

    @Test
    void findModel_caseInsensitiveMethod_shouldFindCorrectly() {
        // Setup
        ApiModel model = new ApiModel("/test", "GET", null, null, null);
        modelStorageService.storeModels(Collections.singletonList(model));

        // Execute - using lowercase "get" instead of "GET"
        Optional<ApiModel> result = modelStorageService.findModel("get", "/test");

        // Verify - should find the model (methods are stored uppercase)
        assertTrue(result.isPresent());
    }

    @Test
    void clearStore_shouldRemoveAllModels() {
        // Setup
        ApiModel model1 = new ApiModel("/test1", "GET", null, null, null);
        ApiModel model2 = new ApiModel("/test2", "POST", null, null, null);
        modelStorageService.storeModels(Arrays.asList(model1, model2));
        assertEquals(2, modelStorageService.getStoreSize());

        // Execute
        modelStorageService.clearStore();

        // Verify
        assertEquals(0, modelStorageService.getStoreSize());
        assertFalse(modelStorageService.findModel("GET", "/test1").isPresent());
        assertFalse(modelStorageService.findModel("POST", "/test2").isPresent());
    }
} 