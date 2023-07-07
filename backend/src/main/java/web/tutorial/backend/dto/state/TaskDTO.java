package web.tutorial.backend.dto.state;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record TaskDTO(
    String id,
    String truck,
    String source,
    String destination,
		@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime created,
		@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
		LocalDateTime started,
		@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
		LocalDateTime deadline,
		@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
		LocalDateTime completed,
		String status) {}