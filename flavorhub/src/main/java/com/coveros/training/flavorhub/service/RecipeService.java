package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service for managing recipes
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
    
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }
    
    public List<Recipe> getRecipesByDifficulty(String difficultyLevel) {
        return recipeRepository.findByDifficultyLevel(difficultyLevel);
    }
    
    public List<Recipe> getRecipesByCuisine(String cuisineType) {
        return recipeRepository.findByCuisineType(cuisineType);
    }
    
    public List<Recipe> searchRecipes(String searchTerm) {
        return recipeRepository.findByNameContainingIgnoreCase(searchTerm);
    }
    
    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }
    
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
    
    /**
     * Add a rating to a recipe and update the average rating
     * 
     * @param recipeId the ID of the recipe to rate
     * @param rating the rating value (1-5)
     * @return the updated recipe
     * @throws IllegalArgumentException if rating is not between 1 and 5
     * @throws java.util.NoSuchElementException if recipe is not found
     */
    public Recipe addRating(Long recipeId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found with id: " + recipeId));
        
        // Calculate new average: ((oldRating * oldCount) + newRating) / (oldCount + 1)
        double currentAverage = recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0;
        int currentCount = recipe.getRatingCount() != null ? recipe.getRatingCount() : 0;
        
        double newAverage = ((currentAverage * currentCount) + rating) / (currentCount + 1);
        
        recipe.setAverageRating(newAverage);
        recipe.setRatingCount(currentCount + 1);
        
        return recipeRepository.save(recipe);
    }
    
    /**
     * Get the recipe of the day based on the current date
     * Uses a deterministic algorithm so the same recipe is returned for the entire day
     * The algorithm uses the day of year modulo the total number of recipes
     * @return Optional containing the recipe of the day, or empty if no recipes exist
     */
    public Optional<Recipe> getDailyRecipe() {
        try {
            List<Recipe> allRecipes = recipeRepository.findAll();
            
            if (allRecipes.isEmpty()) {
                log.warn("No recipes available for Recipe of the Day");
                return Optional.empty();
            }
            
            // Use day of year to ensure same recipe shows all day
            LocalDate today = LocalDate.now();
            int dayOfYear = today.getDayOfYear();
            int recipeIndex = dayOfYear % allRecipes.size();
            
            Recipe dailyRecipe = allRecipes.get(recipeIndex);
            log.info("Selected Recipe of the Day: {} (index: {}, day: {})", 
                    dailyRecipe.getName(), recipeIndex, dayOfYear);
            
            return Optional.of(dailyRecipe);
        } catch (Exception e) {
            log.error("Error fetching Recipe of the Day", e);
            return Optional.empty();
        }
    }
    
    /**
     * Find recipes that can be made based on available ingredients in the pantry
     * NOTE: This method is intentionally left incomplete for workshop participants
     * Participants will use GitHub Copilot to implement this recommendation logic
     */
    // TODO: Implement method to recommend recipes based on pantry ingredients
    
    /**
     * Get recipes that match specific dietary requirements or filters
     * NOTE: This is a more advanced feature to be implemented during the workshop
     */
    // TODO: Implement advanced filtering logic
}
