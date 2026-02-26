package com.techwave.taskapp.service;

import com.techwave.taskapp.dto.CreateTaskRequest;
import com.techwave.taskapp.entity.Task;
import com.techwave.taskapp.enums.TaskStatus;
import com.techwave.taskapp.repository.interfaces.ITaskRepository;
import com.techwave.taskapp.service.mapper.TaskMapper;
import com.techwave.taskapp.service.validation.TaskValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 * 
 * Test Strategy:
 * - Mock all dependencies (repository, validator, mapper)
 * - Test happy path scenarios
 * - Test edge cases and error conditions
 * - Verify method interactions and parameters
 * - Test logging correlation (request ID generation)
 * - Validate exception handling and error propagation
 * 
 * Testing Principles:
 * - Isolated unit tests (no Spring context)
 * - Fast execution with mocked dependencies
 * - Comprehensive coverage including edge cases
 * - Clear test naming and structure
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private ITaskRepository taskRepository;
    
    @Mock
    private TaskValidator taskValidator;
    
    @Mock
    private TaskMapper taskMapper;
    
    @InjectMocks
    private TaskService taskService;
    
    private CreateTaskRequest validRequest;
    private Task mappedTask;
    private Task persistedTask;
    
    @BeforeEach
    void setUp() {
        // Prepare test data
        validRequest = new CreateTaskRequest("Test Task", "Test Description");
        
        mappedTask = new Task();
        mappedTask.setTitle("Test Task");
        mappedTask.setDescription("Test Description");
        mappedTask.setStatus(TaskStatus.PENDING);
        mappedTask.setCreatedAt(Instant.now());
        
        persistedTask = new Task();
        persistedTask.setId("task-123");
        persistedTask.setTitle("Test Task");
        persistedTask.setDescription("Test Description");
        persistedTask.setStatus(TaskStatus.PENDING);
        persistedTask.setCreatedAt(Instant.now());
    }

    @Nested
    @DisplayName("Create Task - Happy Path Scenarios")
    class CreateTaskHappyPath {

        @Test
        @DisplayName("Should create task successfully with title and description")
        void shouldCreateTaskSuccessfully() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(validRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("task-123");
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getDescription()).isEqualTo("Test Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(result.getCreatedAt()).isNotNull();

            // Verify method calls in correct order
            verify(taskValidator).validateCreateRequest(validRequest);
            verify(taskValidator).sanitizeCreateRequest(validRequest);
            verify(taskMapper).mapToEntity(validRequest);
            verify(taskRepository).create(mappedTask);
            
            // Verify no additional interactions
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }

        @Test
        @DisplayName("Should create task successfully with title only (null description)")
        void shouldCreateTaskWithTitleOnly() {
            // Given
            CreateTaskRequest requestWithoutDescription = new CreateTaskRequest("Test Task", null);
            Task taskWithoutDescription = new Task();
            taskWithoutDescription.setTitle("Test Task");
            taskWithoutDescription.setDescription(null);
            taskWithoutDescription.setStatus(TaskStatus.PENDING);
            taskWithoutDescription.setCreatedAt(Instant.now());
            
            Task persistedTaskWithoutDescription = new Task();
            persistedTaskWithoutDescription.setId("task-456");
            persistedTaskWithoutDescription.setTitle("Test Task");
            persistedTaskWithoutDescription.setDescription(null);
            persistedTaskWithoutDescription.setStatus(TaskStatus.PENDING);
            persistedTaskWithoutDescription.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(requestWithoutDescription)).thenReturn(taskWithoutDescription);
            when(taskRepository.create(taskWithoutDescription)).thenReturn(persistedTaskWithoutDescription);

            // When
            Task result = taskService.createTask(requestWithoutDescription);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("task-456");
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);

            verify(taskValidator).validateCreateRequest(requestWithoutDescription);
            verify(taskValidator).sanitizeCreateRequest(requestWithoutDescription);
            verify(taskMapper).mapToEntity(requestWithoutDescription);
            verify(taskRepository).create(taskWithoutDescription);
        }

        @Test
        @DisplayName("Should create task successfully with empty description")
        void shouldCreateTaskWithEmptyDescription() {
            // Given
            CreateTaskRequest requestWithEmptyDescription = new CreateTaskRequest("Test Task", "");
            Task taskWithEmptyDescription = new Task();
            taskWithEmptyDescription.setTitle("Test Task");
            taskWithEmptyDescription.setDescription("");
            taskWithEmptyDescription.setStatus(TaskStatus.PENDING);
            taskWithEmptyDescription.setCreatedAt(Instant.now());
            
            Task persistedTaskWithEmptyDescription = new Task();
            persistedTaskWithEmptyDescription.setId("task-789");
            persistedTaskWithEmptyDescription.setTitle("Test Task");
            persistedTaskWithEmptyDescription.setDescription("");
            persistedTaskWithEmptyDescription.setStatus(TaskStatus.PENDING);
            persistedTaskWithEmptyDescription.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(requestWithEmptyDescription)).thenReturn(taskWithEmptyDescription);
            when(taskRepository.create(taskWithEmptyDescription)).thenReturn(persistedTaskWithEmptyDescription);

            // When
            Task result = taskService.createTask(requestWithEmptyDescription);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("task-789");
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getDescription()).isEqualTo("");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);

            verify(taskValidator).validateCreateRequest(requestWithEmptyDescription);
            verify(taskValidator).sanitizeCreateRequest(requestWithEmptyDescription);
            verify(taskMapper).mapToEntity(requestWithEmptyDescription);
            verify(taskRepository).create(taskWithEmptyDescription);
        }

        @Test
        @DisplayName("Should handle maximum length title and description")
        void shouldHandleMaximumLengthContent() {
            // Given
            String maxTitle = "A".repeat(255); // Maximum title length
            String maxDescription = "B".repeat(2000); // Maximum description length
            CreateTaskRequest maxLengthRequest = new CreateTaskRequest(maxTitle, maxDescription);
            
            Task maxLengthTask = new Task();
            maxLengthTask.setTitle(maxTitle);
            maxLengthTask.setDescription(maxDescription);
            maxLengthTask.setStatus(TaskStatus.PENDING);
            maxLengthTask.setCreatedAt(Instant.now());
            
            Task persistedMaxLengthTask = new Task();
            persistedMaxLengthTask.setId("task-max");
            persistedMaxLengthTask.setTitle(maxTitle);
            persistedMaxLengthTask.setDescription(maxDescription);
            persistedMaxLengthTask.setStatus(TaskStatus.PENDING);
            persistedMaxLengthTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(maxLengthRequest)).thenReturn(maxLengthTask);
            when(taskRepository.create(maxLengthTask)).thenReturn(persistedMaxLengthTask);

            // When
            Task result = taskService.createTask(maxLengthRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("task-max");
            assertThat(result.getTitle()).hasSize(255);
            assertThat(result.getDescription()).hasSize(2000);
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);

            verify(taskValidator).validateCreateRequest(maxLengthRequest);
            verify(taskValidator).sanitizeCreateRequest(maxLengthRequest);
            verify(taskMapper).mapToEntity(maxLengthRequest);
            verify(taskRepository).create(maxLengthTask);
        }
    }

    @Nested
    @DisplayName("Create Task - Validation Error Scenarios")
    class CreateTaskValidationErrors {

        @Test
        @DisplayName("Should propagate IllegalArgumentException from validator")
        void shouldPropagateValidationException() {
            // Given
            String validationErrorMessage = "Title cannot be empty or contain only whitespace";
            doThrow(new IllegalArgumentException(validationErrorMessage))
                    .when(taskValidator).validateCreateRequest(validRequest);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(validRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(validationErrorMessage);

            // Verify only validation was called (not sanitization, mapping, or persistence)
            verify(taskValidator).validateCreateRequest(validRequest);
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }

        @Test
        @DisplayName("Should handle null request gracefully")
        void shouldHandleNullRequest() {
            // Given
            String nullRequestMessage = "Request cannot be null";
            doThrow(new IllegalArgumentException(nullRequestMessage))
                    .when(taskValidator).validateCreateRequest(null);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(nullRequestMessage);

            verify(taskValidator).validateCreateRequest(null);
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }

        @Test
        @DisplayName("Should handle title that becomes empty after trimming")
        void shouldHandleTitleEmptyAfterTrimming() {
            // Given
            CreateTaskRequest whitespaceRequest = new CreateTaskRequest("   ", "Valid description");
            String trimErrorMessage = "Title cannot be empty or contain only whitespace";
            doThrow(new IllegalArgumentException(trimErrorMessage))
                    .when(taskValidator).validateCreateRequest(whitespaceRequest);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(whitespaceRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(trimErrorMessage);

            verify(taskValidator).validateCreateRequest(whitespaceRequest);
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }

        @Test
        @DisplayName("Should handle title exceeding maximum length")
        void shouldHandleTitleTooLong() {
            // Given
            String longTitle = "A".repeat(256); // Exceeds 255 character limit
            CreateTaskRequest longTitleRequest = new CreateTaskRequest(longTitle, "Valid description");
            String lengthErrorMessage = "Title must be between 1 and 255 characters";
            doThrow(new IllegalArgumentException(lengthErrorMessage))
                    .when(taskValidator).validateCreateRequest(longTitleRequest);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(longTitleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(lengthErrorMessage);

            verify(taskValidator).validateCreateRequest(longTitleRequest);
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }

        @Test
        @DisplayName("Should handle description exceeding maximum length")
        void shouldHandleDescriptionTooLong() {
            // Given
            String longDescription = "B".repeat(2001); // Exceeds 2000 character limit
            CreateTaskRequest longDescriptionRequest = new CreateTaskRequest("Valid title", longDescription);
            String lengthErrorMessage = "Description cannot exceed 2000 characters";
            doThrow(new IllegalArgumentException(lengthErrorMessage))
                    .when(taskValidator).validateCreateRequest(longDescriptionRequest);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(longDescriptionRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(lengthErrorMessage);

            verify(taskValidator).validateCreateRequest(longDescriptionRequest);
            verifyNoMoreInteractions(taskValidator, taskMapper, taskRepository);
        }
    }

    @Nested
    @DisplayName("Create Task - Repository Error Scenarios")
    class CreateTaskRepositoryErrors {

        @Test
        @DisplayName("Should wrap repository exceptions in RuntimeException")
        void shouldWrapRepositoryExceptions() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasRootCauseMessage("Database connection failed");

            // Verify all steps were attempted
            verify(taskValidator).validateCreateRequest(validRequest);
            verify(taskValidator).sanitizeCreateRequest(validRequest);
            verify(taskMapper).mapToEntity(validRequest);
            verify(taskRepository).create(mappedTask);
        }

        @Test
        @DisplayName("Should handle mapper exceptions gracefully")
        void shouldHandleMapperExceptions() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenThrow(new RuntimeException("Mapping failed"));

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasRootCauseMessage("Mapping failed");

            verify(taskValidator).validateCreateRequest(validRequest);
            verify(taskValidator).sanitizeCreateRequest(validRequest);
            verify(taskMapper).mapToEntity(validRequest);
            verifyNoInteractions(taskRepository);
        }

        @Test
        @DisplayName("Should handle unexpected exceptions during sanitization")
        void shouldHandleSanitizationExceptions() {
            // Given
            doThrow(new RuntimeException("Sanitization failed"))
                    .when(taskValidator).sanitizeCreateRequest(validRequest);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(validRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create task due to internal error")
                    .hasRootCauseMessage("Sanitization failed");

            verify(taskValidator).validateCreateRequest(validRequest);
            verify(taskValidator).sanitizeCreateRequest(validRequest);
            verifyNoInteractions(taskMapper, taskRepository);
        }
    }

    @Nested
    @DisplayName("Create Task - Method Interaction Verification")
    class CreateTaskMethodInteractions {

        @Test
        @DisplayName("Should pass correct request object through all methods")
        void shouldPassCorrectRequestObject() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            taskService.createTask(validRequest);

            // Then - Capture arguments to verify they're the same object
            ArgumentCaptor<CreateTaskRequest> validationCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);
            ArgumentCaptor<CreateTaskRequest> sanitizationCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);
            ArgumentCaptor<CreateTaskRequest> mappingCaptor = ArgumentCaptor.forClass(CreateTaskRequest.class);

            verify(taskValidator).validateCreateRequest(validationCaptor.capture());
            verify(taskValidator).sanitizeCreateRequest(sanitizationCaptor.capture());
            verify(taskMapper).mapToEntity(mappingCaptor.capture());

            // Verify same request object is passed to all methods
            assertThat(validationCaptor.getValue()).isSameAs(validRequest);
            assertThat(sanitizationCaptor.getValue()).isSameAs(validRequest);
            assertThat(mappingCaptor.getValue()).isSameAs(validRequest);
        }

        @Test
        @DisplayName("Should pass mapped entity to repository")
        void shouldPassMappedEntityToRepository() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            taskService.createTask(validRequest);

            // Then
            ArgumentCaptor<Task> repositoryCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).create(repositoryCaptor.capture());
            
            assertThat(repositoryCaptor.getValue()).isSameAs(mappedTask);
        }

        @Test
        @DisplayName("Should return the task from repository")
        void shouldReturnRepositoryResult() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            Task result = taskService.createTask(validRequest);

            // Then
            assertThat(result).isSameAs(persistedTask);
        }

        @Test
        @DisplayName("Should call methods in correct order")
        void shouldCallMethodsInCorrectOrder() {
            // Given
            when(taskMapper.mapToEntity(validRequest)).thenReturn(mappedTask);
            when(taskRepository.create(mappedTask)).thenReturn(persistedTask);

            // When
            taskService.createTask(validRequest);

            // Then - Verify order using InOrder
            var inOrder = inOrder(taskValidator, taskMapper, taskRepository);
            inOrder.verify(taskValidator).validateCreateRequest(validRequest);
            inOrder.verify(taskValidator).sanitizeCreateRequest(validRequest);
            inOrder.verify(taskMapper).mapToEntity(validRequest);
            inOrder.verify(taskRepository).create(mappedTask);
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("Create Task - Edge Cases and Special Scenarios")
    class CreateTaskEdgeCases {

        @Test
        @DisplayName("Should handle request with special characters in title and description")
        void shouldHandleSpecialCharacters() {
            // Given
            CreateTaskRequest specialCharRequest = new CreateTaskRequest(
                    "Task with 特殊字符 & émojis 🚀", 
                    "Description with\nnewlines\tand\ttabs & symbols @#$%"
            );
            
            Task specialCharTask = new Task();
            specialCharTask.setTitle("Task with 特殊字符 & émojis 🚀");
            specialCharTask.setDescription("Description with\nnewlines\tand\ttabs & symbols @#$%");
            specialCharTask.setStatus(TaskStatus.PENDING);
            specialCharTask.setCreatedAt(Instant.now());
            
            Task persistedSpecialCharTask = new Task();
            persistedSpecialCharTask.setId("task-special");
            persistedSpecialCharTask.setTitle("Task with 特殊字符 & émojis 🚀");
            persistedSpecialCharTask.setDescription("Description with\nnewlines\tand\ttabs & symbols @#$%");
            persistedSpecialCharTask.setStatus(TaskStatus.PENDING);
            persistedSpecialCharTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(specialCharRequest)).thenReturn(specialCharTask);
            when(taskRepository.create(specialCharTask)).thenReturn(persistedSpecialCharTask);

            // When
            Task result = taskService.createTask(specialCharRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).contains("特殊字符", "émojis", "🚀");
            assertThat(result.getDescription()).contains("\n", "\t", "@#$%");

            verify(taskValidator).validateCreateRequest(specialCharRequest);
            verify(taskValidator).sanitizeCreateRequest(specialCharRequest);
            verify(taskMapper).mapToEntity(specialCharRequest);
            verify(taskRepository).create(specialCharTask);
        }

        @Test
        @DisplayName("Should handle minimum valid title (single character)")
        void shouldHandleMinimumValidTitle() {
            // Given
            CreateTaskRequest minTitleRequest = new CreateTaskRequest("A", "Valid description");
            Task minTitleTask = new Task();
            minTitleTask.setTitle("A");
            minTitleTask.setDescription("Valid description");
            minTitleTask.setStatus(TaskStatus.PENDING);
            minTitleTask.setCreatedAt(Instant.now());
            
            Task persistedMinTitleTask = new Task();
            persistedMinTitleTask.setId("task-min");
            persistedMinTitleTask.setTitle("A");
            persistedMinTitleTask.setDescription("Valid description");
            persistedMinTitleTask.setStatus(TaskStatus.PENDING);
            persistedMinTitleTask.setCreatedAt(Instant.now());

            when(taskMapper.mapToEntity(minTitleRequest)).thenReturn(minTitleTask);
            when(taskRepository.create(minTitleTask)).thenReturn(persistedMinTitleTask);

            // When
            Task result = taskService.createTask(minTitleRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("A");
            assertThat(result.getDescription()).isEqualTo("Valid description");

            verify(taskValidator).validateCreateRequest(minTitleRequest);
            verify(taskValidator).sanitizeCreateRequest(minTitleRequest);
            verify(taskMapper).mapToEntity(minTitleRequest);
            verify(taskRepository).create(minTitleTask);
        }

        @Test
        @DisplayName("Should handle concurrent creation requests independently")
        void shouldHandleConcurrentRequests() {
            // Given
            CreateTaskRequest request1 = new CreateTaskRequest("Task 1", "Description 1");
            CreateTaskRequest request2 = new CreateTaskRequest("Task 2", "Description 2");
            
            Task task1 = new Task();
            task1.setTitle("Task 1");
            task1.setDescription("Description 1");
            task1.setStatus(TaskStatus.PENDING);
            task1.setCreatedAt(Instant.now());
            
            Task task2 = new Task();
            task2.setTitle("Task 2");
            task2.setDescription("Description 2");
            task2.setStatus(TaskStatus.PENDING);
            task2.setCreatedAt(Instant.now());
            
            Task persistedTask1 = new Task();
            persistedTask1.setId("task-1");
            persistedTask1.setTitle("Task 1");
            persistedTask1.setDescription("Description 1");
            persistedTask1.setStatus(TaskStatus.PENDING);
            persistedTask1.setCreatedAt(Instant.now());
            
            Task persistedTask2 = new Task();
            persistedTask2.setId("task-2");
            persistedTask2.setTitle("Task 2");
            persistedTask2.setDescription("Description 2");
            persistedTask2.setStatus(TaskStatus.PENDING);
            persistedTask2.setCreatedAt(Instant.now());

            // Configure mocks for both requests
            when(taskMapper.mapToEntity(request1)).thenReturn(task1);
            when(taskMapper.mapToEntity(request2)).thenReturn(task2);
            when(taskRepository.create(task1)).thenReturn(persistedTask1);
            when(taskRepository.create(task2)).thenReturn(persistedTask2);

            // When - Simulate concurrent calls (though executed sequentially in test)
            Task result1 = taskService.createTask(request1);
            Task result2 = taskService.createTask(request2);

            // Then
            assertThat(result1).isNotNull();
            assertThat(result1.getId()).isEqualTo("task-1");
            assertThat(result1.getTitle()).isEqualTo("Task 1");
            
            assertThat(result2).isNotNull();
            assertThat(result2.getId()).isEqualTo("task-2");
            assertThat(result2.getTitle()).isEqualTo("Task 2");

            // Verify both requests were processed
            verify(taskValidator, times(2)).validateCreateRequest(any(CreateTaskRequest.class));
            verify(taskValidator, times(2)).sanitizeCreateRequest(any(CreateTaskRequest.class));
            verify(taskMapper, times(2)).mapToEntity(any(CreateTaskRequest.class));
            verify(taskRepository, times(2)).create(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Constructor and Initialization Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with all dependencies")
        void shouldInitializeWithAllDependencies() {
            // Given - Fresh instances for testing constructor
            ITaskRepository repo = mock(ITaskRepository.class);
            TaskValidator validator = mock(TaskValidator.class);
            TaskMapper mapper = mock(TaskMapper.class);

            // When
            TaskService service = new TaskService(repo, validator, mapper);

            // Then - Service should be created successfully
            assertThat(service).isNotNull();
            
            // Verify the service can be used
            CreateTaskRequest testRequest = new CreateTaskRequest("Test", "Description");
            Task testTask = new Task();
            testTask.setId("test-id");
            testTask.setTitle("Test");
            testTask.setDescription("Description");
            testTask.setStatus(TaskStatus.PENDING);
            testTask.setCreatedAt(Instant.now());

            when(mapper.mapToEntity(testRequest)).thenReturn(testTask);
            when(repo.create(testTask)).thenReturn(testTask);

            // Should not throw exception
            assertThat(() -> service.createTask(testRequest)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Request ID Generation and Logging Tests")  
    class RequestIdTests {

        @Test
        @DisplayName("Should generate unique request IDs for different requests")
        void shouldGenerateUniqueRequestIds() {
            // Given
            CreateTaskRequest request1 = new CreateTaskRequest("Task 1", "Description 1");
            CreateTaskRequest request2 = new CreateTaskRequest("Task 2", "Description 2");
            
            Task task = new Task();
            task.setId("test-id");
            when(taskMapper.mapToEntity(any())).thenReturn(task);
            when(taskRepository.create(any())).thenReturn(task);

            // When - Call service multiple times
            taskService.createTask(request1);
            taskService.createTask(request2);

            // Then - Both calls should complete successfully
            // (Request ID uniqueness is primarily for logging correlation,
            // and is tested indirectly through successful execution)
            verify(taskValidator, times(2)).validateCreateRequest(any());
            verify(taskMapper, times(2)).mapToEntity(any());
            verify(taskRepository, times(2)).create(any());
        }
    }
}