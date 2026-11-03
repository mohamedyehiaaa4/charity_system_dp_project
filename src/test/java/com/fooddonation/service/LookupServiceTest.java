package com.fooddonation.service;

import com.fooddonation.model.lookup.FoodCategory;
import com.fooddonation.model.lookup.LookupEntry;
import com.fooddonation.model.lookup.LookupType;
import com.fooddonation.model.user.Coordinator;
import com.fooddonation.model.user.User;
import com.fooddonation.persistence.LookupRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LookupServiceTest {
    private final LookupRepository repository = mock(LookupRepository.class);
    private final LookupService lookups = new LookupService(repository);
    private final User coordinator = new Coordinator();

    @Test
    void getAllReadsFromTheRepository() {
        List<LookupEntry> entries = List.of(new FoodCategory(1, "Bakery", false));
        when(repository.readAll(LookupType.FOOD_CATEGORY)).thenReturn(entries);

        assertEquals(entries, lookups.getAll(LookupType.FOOD_CATEGORY));
    }

    @Test
    void createReturnsTheSavedEntry() {
        LookupEntry saved = new FoodCategory(7, "Snacks", false);
        when(repository.create(coordinator, LookupType.FOOD_CATEGORY, "Snacks")).thenReturn(saved);

        assertSame(saved, lookups.create(coordinator, LookupType.FOOD_CATEGORY, "Snacks"));
    }

    @Test
    void renameAndDeleteArePassedOn() {
        lookups.rename(coordinator, LookupType.STORAGE_TYPE, 3, "Chilled");
        lookups.delete(coordinator, LookupType.STORAGE_TYPE, 3);

        verify(repository).update(coordinator, LookupType.STORAGE_TYPE, 3, "Chilled");
        verify(repository).delete(coordinator, LookupType.STORAGE_TYPE, 3);
    }
}
