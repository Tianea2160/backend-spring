package com.teamteam.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaRepositories(basePackages = ["com.teamteam.backend.domain"])
@Configuration
class JpaConfig