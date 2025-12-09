package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {
    
    @Mock
    private RecipeRepository recipeRepository;
    
    @InjectMocks
    private RecipeService recipeService;
    
    private Recipe testRecipe;
    
    @BeforeEach
    void setUp() {
        testRecipe = new Recipe("Test Recipe", "A test recipe", 10, 20, 4, "Easy", "Italian");
        testRecipe.setId(1L);
        testRecipe.setAverageRating(0.0);
        testRecipe.setRatingCount(0);
    }
    
    @Test
    void testAddRating_WhenFirstRating_ThenSetsCorrectAverage() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Recipe result = recipeService.addRating(1L, 5);
        
        // Assert
        assertEquals(5.0, result.getAverageRating(), 0.001);
        assertEquals(1, result.getRatingCount());
        verify(recipeRepository).save(testRecipe);
    }
    
    @Test
    void testAddRating_WhenMultipleRatings_ThenCalculatesCorrectAverage() {
        // Arrange
        testRecipe.setAverageRating(4.0);
        testRecipe.setRatingCount(2);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Recipe result = recipeService.addRating(1L, 5);
        
        // Assert
        // Expected: ((4.0 * 2) + 5) / 3 = 13 / 3 = 4.333...
        assertEquals(4.333, result.getAverageRating(), 0.01);
        assertEquals(3, result.getRatingCount());
    }
    
    @Test
    void testAddRating_WhenRatingBelowOne_ThenThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.addRating(1L, 0);
        });
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRatingAboveFive_ThenThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.addRating(1L, 6);
        });
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRecipeNotFound_ThenThrowsException() {
        // Arrange
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            recipeService.addRating(999L, 5);
        });
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenValidRatingOfOne_ThenSucceeds() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Recipe result = recipeService.addRating(1L, 1);
        
        // Assert
        assertEquals(1.0, result.getAverageRating(), 0.001);
        assertEquals(1, result.getRatingCount());
    }
    
    @Test
    void testAddRating_WhenNullRatings_ThenHandlesGracefully() {
        // Arrange
        testRecipe.setAverageRating(null);
        testRecipe.setRatingCount(null);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Recipe result = recipeService.addRating(1L, 4);
        
        // Assert
        assertEquals(4.0, result.getAverageRating(), 0.001);
        assertEquals(1, result.getRatingCount());
    }
}
