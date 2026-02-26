package com.techwave.taskapp.service;

import com.techwave.taskapp.dto.CreateTaskRequest;
import com.techwave.taskapp.entity.Task;
import com.techwave.taskapp.enums.TaskStatus;
import com.techwave.taskapp.repository.interfaces.ITaskRepository;
import com.techwave.taskapp.service.mapper.TaskMapper;
import com.techwave.taskapp.service.validation.TaskValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Edge Cases and Negative Test Scenarios for TaskService.
 * 
 * This test suite focuses on:
 * - Boundary value testing
 * - Invalid input validation
 * - Error condition handling
 * - Performance edge cases
 * - Security considerations
 * - Resource limitation scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Edge Cases and Negative Scenarios")
class TaskServiceEdgeCasesTest {

    @Mock
    private ITaskRepository taskRepository;
    
    @Mock
    private TaskValidator taskValidator;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskService taskService;

    @Nested
    @DisplayName("Boundary Value Testing")
    class BoundaryValueTests {

        @ParameterizedTest(name = "Should handle title length: {0}")
        @ValueSource(ints = {1, 2, 100, 254, 255})
        @DisplayName("Should handle various valid title lengths")
        void shouldHandleValidTitleLengths(int titleLength) {
            // Given
            String title = "A".repeat(titleLength);
            CreateTaskRequest request = new CreateTaskRequest(title, "Valid description");
            
            Task mappedTask = new Task();
            mappedTask.setTitle(title);
            mappedTask.setDescription("Valid description");
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-" + titleLength);
            persistedTask.setTitle(title);
            persistedTask.setDescription("Valid description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).hasSize(titleLength);
            assertThat(result.getId()).isEqualTo("task-" + titleLength);
        }

        @ParameterizedTest(name = "Should handle description length: {0}")
        @ValueSource(ints = {0, 1, 100, 1000, 1999, 2000})
        @DisplayName("Should handle various valid description lengths")
        void shouldHandleValidDescriptionLengths(int descriptionLength) {
            // Given
            String description = descriptionLength == 0 ? "" : "B".repeat(descriptionLength);
            CreateTaskRequest request = new CreateTaskRequest("Valid title", description);
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Valid title");
            mappedTask.setDescription(description);
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-desc-" + descriptionLength);
            persistedTask.setTitle("Valid title");
            persistedTask.setDescription(description);
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).hasSize(descriptionLength);
            assertThat(result.getId()).isEqualTo("task-desc-" + descriptionLength);
        }

        @ParameterizedTest(name = "Should reject title length: {0}")
        @ValueSource(ints = {256, 300, 1000, 5000})
        @DisplayName("Should reject titles exceeding maximum length")
        void shouldRejectOversizedTitles(int titleLength) {
            // Given
            String oversizedTitle = "A".repeat(titleLength);
            CreateTaskRequest request = new CreateTaskRequest(oversizedTitle, "Valid description");
            
            String errorMessage = "Title must be between 1 and 255 characters";
            doThrow(new IllegalArgumentException(errorMessage))
                    .when(taskValidator).validateCreateRequest(request);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(errorMessage);

            verify(taskValidator).validateCreateRequest(request);
            verifyNoInteractions(taskMapper, taskRepository);
        }

        @ParameterizedTest(name = "Should reject description length: {0}")
        @ValueSource(ints = {2001, 3000, 5000, 10000})
        @DisplayName("Should reject descriptions exceeding maximum length")
        void shouldRejectOversizedDescriptions(int descriptionLength) {
            // Given
            String oversizedDescription = "B".repeat(descriptionLength);
            CreateTaskRequest request = new CreateTaskRequest("Valid title", oversizedDescription);
            
            String errorMessage = "Description cannot exceed 2000 characters";
            doThrow(new IllegalArgumentException(errorMessage))
                    .when(taskValidator).validateCreateRequest(request);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(errorMessage);

            verify(taskValidator).validateCreateRequest(request);
            verifyNoInteractions(taskMapper, taskRepository);
        }
    }

    @Nested
    @DisplayName("Whitespace and Empty String Handling")
    class WhitespaceHandlingTests {

        @ParameterizedTest(name = "Should reject whitespace-only title: '{0}'")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n", "\r\n", "   \t\n   "})
        @DisplayName("Should reject null, empty, and whitespace-only titles")
        void shouldRejectInvalidTitles(String invalidTitle) {
            // Given
            CreateTaskRequest request = new CreateTaskRequest(invalidTitle, "Valid description");
            
            String errorMessage = invalidTitle == null ? 
                    "Title is required and cannot be empty or whitespace only" :
                    "Title cannot be empty or contain only whitespace";
            doThrow(new IllegalArgumentException(errorMessage))
                    .when(taskValidator).validateCreateRequest(request);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Title");

            verify(taskValidator).validateCreateRequest(request);
            verifyNoInteractions(taskMapper, taskRepository);
        }

        @ParameterizedTest(name = "Should handle description value: '{0}'")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n", "\r\n", "   \t\n   "})
        @DisplayName("Should handle null, empty, and whitespace-only descriptions")
        void shouldHandleVariousDescriptions(String description) {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Valid title", description);
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Valid title");
            mappedTask.setDescription(description);
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-whitespace");
            persistedTask.setTitle("Valid title");
            persistedTask.setDescription(description);
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Valid title");
            // Description handling is delegated to validator and mapper
            
            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }
    }

    @Nested
    @DisplayName("Special Character and Encoding Tests")
    class SpecialCharacterTests {

        static Stream<Arguments> specialCharacterTitles() {
            return Stream.of(
                Arguments.of("Unicode: 你好世界", "Chinese characters"),
                Arguments.of("Émojis: 🎉🚀💯", "Emojis"),
                Arguments.of("Symbols: @#$%^&*()", "Special symbols"),
                Arguments.of("Math: ∑∆∞±≈≠", "Mathematical symbols"),
                Arguments.of("Currency: $€£¥₹", "Currency symbols"),
                Arguments.of("Quotes: \"'`´", "Various quotes"),
                Arguments.of("Brackets: []{}()<>", "Various brackets"),
                Arguments.of("Escapes: \\\n\t\r", "Escape characters"),
                Arguments.of("Mixed: café naïve résumé", "Accented characters"),
                Arguments.of("RTL: العربية עברית", "Right-to-left text")
            );
        }

        @ParameterizedTest(name = "Should handle {1}: {0}")
        @MethodSource("specialCharacterTitles")
        @DisplayName("Should handle titles with special characters")
        void shouldHandleSpecialCharactersInTitle(String title, String description) {
            // Given
            CreateTaskRequest request = new CreateTaskRequest(title, "Standard description");
            
            Task mappedTask = new Task();
            mappedTask.setTitle(title);
            mappedTask.setDescription("Standard description");
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-special");
            persistedTask.setTitle(title);
            persistedTask.setDescription("Standard description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(title);
            assertThat(result.getDescription()).isEqualTo("Standard description");

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }

        @Test
        @DisplayName("Should handle multiline description")
        void shouldHandleMultilineDescription() {
            // Given
            String multilineDescription = "Line 1\nLine 2\r\nLine 3\n\nLine 5";
            CreateTaskRequest request = new CreateTaskRequest("Multiline Task", multilineDescription);
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Multiline Task");
            mappedTask.setDescription(multilineDescription);
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-multiline");
            persistedTask.setTitle("Multiline Task");
            persistedTask.setDescription(multilineDescription);
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDescription())
                    .contains("\n")
                    .contains("\r\n")
                    .contains("Line 1", "Line 2", "Line 3", "Line 5");

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }
    }

    @Nested
    @DisplayName("Error Propagation and Exception Chaining")
    class ErrorPropagationTests {

        @Test
        @DisplayName("Should preserve original validation error message")
        void shouldPreserveValidationErrorMessage() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("", "Description");
            String originalMessage = "Custom validation error from TaskValidator";
            doThrow(new IllegalArgumentException(originalMessage))
                    .when(taskValidator).validateCreateRequest(request);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(originalMessage)
                    .hasNoCause(); // Validation errors should not be wrapped

            verify(taskValidator).validateCreateRequest(request);
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }

        @Test
        @DisplayName("Should wrap non-validation exceptions with meaningful message")
        void shouldWrapNonValidationExceptions() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Valid Title", "Valid description");
            RuntimeException originalException = new RuntimeException("Original failure reason");
            
            when(taskMapper.mapToEntity(request)).thenThrow(originalException);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasCause(originalException)
                    .hasRootCauseMessage("Original failure reason");

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("Should handle chained exceptions properly")
        void shouldHandleChainedExceptions() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Valid Title", "Valid description");
            RuntimeException rootCause = new RuntimeException("Root cause");
            RuntimeException intermediateCause = new RuntimeException("Intermediate cause", rootCause);
            
            Task mappedTask = new Task();
            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenThrow(intermediateCause);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasCause(intermediateCause)
                    .hasRootCauseMessage("Root cause");

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }
    }

    @Nested
    @DisplayName("Resource Limitation Scenarios")
    class ResourceLimitationTests {

        @Test
        @DisplayName("Should handle memory pressure during task creation")
        void shouldHandleMemoryPressure() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Memory Test", 
                    "Description that might cause memory issues during processing");
            
            when(taskMapper.mapToEntity(request))
                    .thenThrow(new OutOfMemoryError("Insufficient memory"));

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasRootCauseInstanceOf(OutOfMemoryError.class);

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("Should handle repository timeout scenarios")
        void shouldHandleRepositoryTimeout() {
            // Given
            CreateTaskRequest request = new CreateTaskRequest("Timeout Test", "Description");
            Task mappedTask = new Task();
            
            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask))
                    .thenThrow(new RuntimeException("Operation timed out after 30 seconds"));

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasRootCauseMessage("Operation timed out after 30 seconds");

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }
    }

    @Nested
    @DisplayName("Security and Input Sanitization Edge Cases")
    class SecurityTests {

        @Test
        @DisplayName("Should handle potential injection attempts in title")
        void shouldHandlePotentialInjectionInTitle() {
            // Given
            String suspiciousTitle = "<script>alert('xss')</script>";
            CreateTaskRequest request = new CreateTaskRequest(suspiciousTitle, "Normal description");
            
            Task mappedTask = new Task();
            mappedTask.setTitle(suspiciousTitle);
            mappedTask.setDescription("Normal description");
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-security");
            persistedTask.setTitle(suspiciousTitle);
            persistedTask.setDescription("Normal description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then - Service should complete successfully
            // Security sanitization is responsibility of TaskValidator
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(suspiciousTitle);

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request); // Sanitization happens here
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }

        @Test
        @DisplayName("Should handle SQL injection-like strings")
        void shouldHandleSqlInjectionLikeStrings() {
            // Given
            String sqlInjectionAttempt = "'; DROP TABLE tasks; --";
            CreateTaskRequest request = new CreateTaskRequest("Normal title", sqlInjectionAttempt);
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Normal title");
            mappedTask.setDescription(sqlInjectionAttempt);
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-sql");
            persistedTask.setTitle("Normal title");
            persistedTask.setDescription(sqlInjectionAttempt);
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(request)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(request);

            // Then - Service layer doesn't handle SQL injection prevention
            // That's the responsibility of the repository layer
            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo(sqlInjectionAttempt);

            verify(taskValidator).validateCreateRequest(request);
            verify(taskValidator).sanitizeCreateRequest(request);
            verify(taskMapper).mapToEntity(request);
            verify(taskRepository).create(mappedTask);
        }
    }

    @Nested
    @DisplayName("Performance Edge Cases")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle rapid successive calls efficiently")
        void shouldHandleRapidSuccessiveCalls() {
            // Given
            int numberOfCalls = 10;
            CreateTaskRequest baseRequest = new CreateTaskRequest("Base Task", "Base Description");
            
            Task mappedTask = new Task();
            mappedTask.setTitle("Base Task");
            mappedTask.setDescription("Base Description");
            mappedTask.setStatus(TaskStatus.PENDING);
            mappedTask.setCreatedAt(Instant.now());
            
            Task persistedTask = new Task();
            persistedTask.setId("task-performance");
            persistedTask.setTitle("Base Task");
            persistedTask.setDescription("Base Description");
            persistedTask.setStatus(TaskStatus.PENDING);
            persistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(any(CreateTaskRequest.class))).thenReturn(mappedTask);
            when(taskRepository.create(any(Task.class))).thenReturn(persistedTask);

            // When - Simulate rapid calls
            for (int i = 0; i < numberOfCalls; i++) {
                CreateTaskRequest request = new CreateTaskRequest(
                        "Task " + i, 
                        "Description " + i
                );
                Task result = taskService.createTask(request);
                assertThat(result).isNotNull();
            }

            // Then
            verify(taskValidator, times(numberOfCalls)).validateCreateRequest(any(CreateTaskRequest.class));
            verify(taskValidator, times(numberOfCalls)).sanitizeCreateRequest(any(CreateTaskRequest.class));
            verify(taskMapper, times(numberOfCalls)).mapToEntity(any(CreateTaskRequest.class));
            verify(taskRepository, times(numberOfCalls)).create(any(Task.class));
        }

        @Test
        @DisplayName("Should handle large content efficiently")
        void shouldHandleLargeContentEfficiently() {
            // Given - Maximum allowed content size
            String maxTitle = "T".repeat(255);
            String maxDescription = "D".repeat(2000);
            CreateTaskRequest largeRequest = new CreateTaskRequest(maxTitle, maxDescription);
            
            Task largeMappedTask = new Task();
            largeMappedTask.setTitle(maxTitle);
            largeMappedTask.setDescription(maxDescription);
            largeMappedTask.setStatus(TaskStatus.PENDING);
            largeMappedTask.setCreatedAt(Instant.now());
            
            Task largePersistedTask = new Task();
            largePersistedTask.setId("task-large");
            largePersistedTask.setTitle(maxTitle);
            largePersistedTask.setDescription(maxDescription);
            largePersistedTask.setStatus(TaskStatus.PENDING);
            largePersistedTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(largeRequest)).thenReturn(largeMappedTask);
            when(taskRepository.create(largeMappedTask)).thenReturn(largePersistedTask);

            // When
            long startTime = System.nanoTime();
            Task result = taskService.createTask(largeRequest);
            long durationNanos = System.nanoTime() - startTime;

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).hasSize(255);
            assertThat(result.getDescription()).hasSize(2000);
            
            // Performance assertion (should complete within reasonable time)
            assertThat(durationNanos).isLessThan(100_000_000L); // Less than 100ms

            verify(taskValidator).validateCreateRequest(largeRequest);
            verify(taskValidator).sanitizeCreateRequest(largeRequest);
            verify(taskMapper).mapToEntity(largeRequest);
            verify(taskRepository).create(largeMappedTask);
        }
    }
}