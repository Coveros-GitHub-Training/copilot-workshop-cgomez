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
 * Service layer component responsible for managing recipe business logic and operations.
 * 
 * <p>This service provides comprehensive recipe management functionality including CRUD operations,
 * search and filtering capabilities, rating management, and the "Recipe of the Day" feature.
 * It acts as an intermediary between the REST controllers and the data access layer, implementing
 * business rules and data validation.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Recipe retrieval and search operations</li>
 *   <li>Recipe creation, update, and deletion</li>
 *   <li>Filtering by difficulty level and cuisine type</li>
 *   <li>Rating management with average calculation</li>
 *   <li>Deterministic "Recipe of the Day" selection</li>
 * </ul>
 * 
 * <p><strong>Transaction Management:</strong><br>
 * All methods in this service are transactional (via {@code @Transactional} at class level),
 * ensuring data consistency across database operations. Write operations will be rolled back
 * automatically if exceptions occur.</p>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * {@code
 * @RestController
 * @RequiredArgsConstructor
 * public class RecipeController {
 *     private final RecipeService recipeService;
 *     
 *     @GetMapping("/api/recipes")
 *     public ResponseEntity<List<Recipe>> getAllRecipes() {
 *         return ResponseEntity.ok(recipeService.getAllRecipes());
 *     }
 * }
 * }
 * </pre>
 * 
 * <p><strong>Workshop Note:</strong><br>
 * Some methods are intentionally incomplete (marked with TODO comments) for workshop participants
 * to implement using GitHub Copilot. These include pantry-based recommendations and advanced
 * filtering capabilities.</p>
 *
 * @author FlavorHub Development Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Recipe
 * @see RecipeRepository
 * @see com.coveros.training.flavorhub.controller.RecipeController
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecipeService {
    
    /**
     * Repository for accessing recipe data from the database.
     * Injected via constructor using Lombok's {@code @RequiredArgsConstructor}.
     */
    private final RecipeRepository recipeRepository;
    
    /**
     * Retrieves all recipes from the database.
     * 
     * <p>This method fetches all available recipes without any filtering or pagination.
     * For large datasets, consider implementing pagination to improve performance.</p>
     *
     * @return a list of all Recipe entities; returns an empty list if no recipes exist
     * @see Recipe
     */
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
    
    /**
     * Retrieves a single recipe by its unique identifier.
     * 
     * <p>This method performs a direct lookup by primary key. The returned Optional
     * will be empty if no recipe exists with the specified ID.</p>
     * 
     * <p><strong>Usage Example:</strong></p>
     * <pre>
     * {@code
     * Optional<Recipe> recipe = recipeService.getRecipeById(1L);
     * recipe.ifPresent(r -> System.out.println("Found: " + r.getName()));
     * }
     * </pre>
     *
     * @param id the unique identifier of the recipe to retrieve; must not be null
     * @return an Optional containing the recipe if found, or empty if not found
     * @see Recipe#getId()
     */
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }
    
    /**
     * Retrieves all recipes matching the specified difficulty level.
     * 
     * <p>This method performs a case-sensitive search for recipes with the exact difficulty
     * level specified. The difficulty levels are typically: "Easy", "Medium", or "Hard".</p>
     * 
     * <p><strong>Expected Values:</strong></p>
     * <ul>
     *   <li>"Easy" - Simple recipes suitable for beginners</li>
     *   <li>"Medium" - Recipes requiring moderate cooking skills</li>
     *   <li>"Hard" - Complex recipes for experienced cooks</li>
     * </ul>
     *
     * @param difficultyLevel the difficulty level to filter by (e.g., "Easy", "Medium", "Hard");
     *                        must not be null or empty
     * @return a list of recipes matching the specified difficulty level; returns an empty list
     *         if no matching recipes are found
     * @see Recipe#getDifficultyLevel()
     */
    public List<Recipe> getRecipesByDifficulty(String difficultyLevel) {
        return recipeRepository.findByDifficultyLevel(difficultyLevel);
    }
    
    /**
     * Retrieves all recipes belonging to a specific cuisine type.
     * 
     * <p>This method performs a case-sensitive search for recipes with the exact cuisine
     * type specified. Common cuisine types include Italian, Mexican, Asian, Indian, etc.</p>
     * 
     * <p><strong>Common Cuisine Types:</strong></p>
     * <ul>
     *   <li>"Italian" - Pasta, pizza, risotto</li>
     *   <li>"Mexican" - Tacos, enchiladas, burritos</li>
     *   <li>"Asian" - Stir-fries, noodles, curry</li>
     *   <li>"American" - Burgers, BBQ, comfort food</li>
     *   <li>"Indian" - Curry, tandoori, biryani</li>
     * </ul>
     *
     * @param cuisineType the cuisine type to filter by (e.g., "Italian", "Mexican", "Asian");
     *                    must not be null or empty
     * @return a list of recipes matching the specified cuisine type; returns an empty list
     *         if no matching recipes are found
     * @see Recipe#getCuisineType()
     */
    public List<Recipe> getRecipesByCuisine(String cuisineType) {
        return recipeRepository.findByCuisineType(cuisineType);
    }
    
    /**
     * Searches for recipes by name using a case-insensitive partial match.
     * 
     * <p>This method performs a flexible search that matches recipes containing the search term
     * anywhere in their name, regardless of case. For example, searching for "pasta" will match
     * "Spaghetti Carbonara", "Pasta Primavera", and "Creamy Pasta".</p>
     * 
     * <p><strong>Search Behavior:</strong></p>
     * <ul>
     *   <li>Case-insensitive ("pasta" matches "PASTA" and "Pasta")</li>
     *   <li>Partial matching ("spaghetti" matches "Spaghetti Carbonara")</li>
     *   <li>SQL LIKE pattern: {@code %searchTerm%}</li>
     * </ul>
     * 
     * <p><strong>Usage Example:</strong></p>
     * <pre>
     * {@code
     * // Search for all recipes containing "chicken"
     * List<Recipe> chickenRecipes = recipeService.searchRecipes("chicken");
     * }
     * </pre>
     *
     * @param searchTerm the text to search for in recipe names; must not be null
     *                   (empty string will return all recipes)
     * @return a list of recipes whose names contain the search term; returns an empty list
     *         if no matches are found
     * @see Recipe#getName()
     */
    public List<Recipe> searchRecipes(String searchTerm) {
        return recipeRepository.findByNameContainingIgnoreCase(searchTerm);
    }
    
    /**
     * Saves a new recipe or updates an existing one.
     * 
     * <p>This method handles both create and update operations. If the recipe has no ID
     * (or ID is null), a new recipe will be created. If the recipe has an existing ID,
     * the corresponding database record will be updated.</p>
     * 
     * <p><strong>Create vs Update:</strong></p>
     * <ul>
     *   <li><strong>Create:</strong> recipe.getId() == null → new database record created</li>
     *   <li><strong>Update:</strong> recipe.getId() != null → existing record updated</li>
     * </ul>
     * 
     * <p><strong>Transactional Behavior:</strong><br>
     * This method runs within a transaction. If an exception occurs, all changes will be
     * rolled back automatically.</p>
     * 
     * <p><strong>Usage Example:</strong></p>
     * <pre>
     * {@code
     * // Create new recipe
     * Recipe newRecipe = new Recipe();
     * newRecipe.setName("Pasta Carbonara");
     * newRecipe.setDifficultyLevel("Medium");
     * Recipe saved = recipeService.saveRecipe(newRecipe);
     * 
     * // Update existing recipe
     * saved.setCuisineType("Italian");
     * recipeService.saveRecipe(saved);
     * }
     * </pre>
     *
     * @param recipe the recipe entity to save or update; must not be null
     * @return the saved recipe with generated ID (for new recipes) and updated fields
     * @see Recipe
     */
    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }
    
    /**
     * Deletes a recipe from the database by its unique identifier.
     * 
     * <p>This method performs a hard delete, permanently removing the recipe and all
     * associated data from the database. If no recipe exists with the specified ID,
     * the operation completes without error (idempotent behavior).</p>
     * 
     * <p><strong>Important Considerations:</strong></p>
     * <ul>
     *   <li>Deletion is permanent and cannot be undone</li>
     *   <li>Associated recipe ingredients may also be deleted (depending on cascade settings)</li>
     *   <li>No exception is thrown if the recipe doesn't exist</li>
     *   <li>This operation runs within a transaction</li>
     * </ul>
     * 
     * <p><strong>Transactional Behavior:</strong><br>
     * If an error occurs during deletion, the transaction will be rolled back.</p>
     *
     * @param id the unique identifier of the recipe to delete; must not be null
     * @see Recipe#getId()
     */
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
    
    /**
     * Adds a user rating to a recipe and recalculates the average rating.
     * 
     * <p>This method implements a rolling average calculation to maintain the recipe's
     * overall rating as new ratings are submitted. The algorithm ensures accurate
     * averages without storing individual rating records.</p>
     * 
     * <p><strong>Rating Calculation:</strong></p>
     * <pre>
     * newAverage = ((currentAverage × currentCount) + newRating) / (currentCount + 1)
     * </pre>
     * 
     * <p><strong>Example:</strong><br>
     * If a recipe has an average of 4.0 from 3 ratings and receives a new rating of 5:
     * <pre>
     * newAverage = ((4.0 × 3) + 5) / (3 + 1) = 17 / 4 = 4.25
     * </pre>
     * 
     * <p><strong>Validation Rules:</strong></p>
     * <ul>
     *   <li>Rating must be between 1 and 5 (inclusive)</li>
     *   <li>Recipe must exist in the database</li>
     *   <li>Rating value must not be null</li>
     * </ul>
     * 
     * <p><strong>Usage Example:</strong></p>
     * <pre>
     * {@code
     * try {
     *     Recipe updated = recipeService.addRating(1L, 5);
     *     System.out.println("New average: " + updated.getAverageRating());
     * } catch (IllegalArgumentException e) {
     *     // Handle invalid rating value
     * } catch (NoSuchElementException e) {
     *     // Handle recipe not found
     * }
     * }
     * </pre>
     * 
     * @param recipeId the unique identifier of the recipe to rate; must not be null
     * @param rating the rating value to add; must be between 1 and 5 (inclusive)
     * @return the updated recipe entity with new average rating and rating count
     * @throws IllegalArgumentException if rating is not between 1 and 5
     * @throws NoSuchElementException if no recipe exists with the specified ID
     * @see Recipe#getAverageRating()
     * @see Recipe#getRatingCount()
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
     * Retrieves the "Recipe of the Day" based on the current date.
     * 
     * <p>This method implements a deterministic algorithm that ensures the same recipe
     * is displayed throughout the entire day, regardless of how many times the method
     * is called. The selection rotates daily, providing variety over time.</p>
     * 
     * <p><strong>Algorithm:</strong></p>
     * <pre>
     * 1. Get all available recipes from the database
     * 2. Calculate day of year (1-365 or 1-366 for leap years)
     * 3. Calculate: recipeIndex = dayOfYear % totalRecipes
     * 4. Return the recipe at that index
     * </pre>
     * 
     * <p><strong>Deterministic Behavior:</strong><br>
     * Because the selection is based on the day of year and total recipe count,
     * the same recipe will be returned for all requests on the same calendar day.
     * This ensures consistency across multiple page loads and API calls.</p>
     * 
     * <p><strong>Example Rotation:</strong></p>
     * <ul>
     *   <li>Day 1 (Jan 1): Recipe at index 1 % 10 = Recipe 1</li>
     *   <li>Day 2 (Jan 2): Recipe at index 2 % 10 = Recipe 2</li>
     *   <li>Day 11 (Jan 11): Recipe at index 11 % 10 = Recipe 1 (cycles back)</li>
     * </ul>
     * 
     * <p><strong>Error Handling:</strong><br>
     * If no recipes exist in the database, an empty Optional is returned and a warning
     * is logged. If an unexpected error occurs during retrieval, the exception is logged
     * and an empty Optional is returned to prevent application failure.</p>
     * 
     * <p><strong>Logging:</strong><br>
     * This method logs the selected recipe at INFO level and any errors at ERROR level
     * for monitoring and debugging purposes.</p>
     *
     * @return an Optional containing the recipe of the day if recipes exist and selection
     *         succeeds; empty Optional if no recipes are available or an error occurs
     * @see Recipe
     * @see LocalDate#getDayOfYear()
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
