package com.safevision.authservice.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.safevision.authservice.dto.UserResponse;

class ModelCoverageTest {

	@Test
	@DisplayName("Coverage: UserResponse DTO")
	void testUserResponseDto() {
	    // 1. Cenário de Sucesso
	    var roles = Set.of("USER", "ADMIN");
	    var response = new UserResponse("uuid-123", "fabio_dev", roles);

	    assertThat(response.id()).isEqualTo("uuid-123");
	    assertThat(response.username()).isEqualTo("fabio_dev");
	    assertThat(response.roles()).containsAll(roles);

	    // 2. Cenário de Erro: ID nulo (Valida o throw no construtor compacto)
	    assertThrows(IllegalArgumentException.class, () -> {
	        new UserResponse(null, "user", Set.of());
	    });

	    // 3. Cenário de Erro: Username vazio
	    assertThrows(IllegalArgumentException.class, () -> {
	        new UserResponse("id", "", Set.of());
	    });
	}
}