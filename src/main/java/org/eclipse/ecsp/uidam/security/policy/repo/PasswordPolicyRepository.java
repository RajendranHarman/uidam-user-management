/*
 * Copyright (c) 2023 - 2024 Harman International
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package org.eclipse.ecsp.uidam.security.policy.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing password policies.
 */
@Repository
public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, BigInteger> { 

    /**
     * Find a password policy by its unique key.
     *
     * @param key The key of the password policy.
     * @return An optional containing the password policy if found.
     */
    Optional<PasswordPolicy> findByKey(String key);
    
    /**
     * Find all password policies that are required, ordered by priority in ascending order.
     *
     * @return A list of required password policies ordered by priority.
     */
    Optional<List<PasswordPolicy>> findAllByRequiredTrueOrderByPriorityAsc();

    /**
     * Fetch the latest update date from the password policies.
     *
     * @return The latest update date as a Timestamp.
     */
    @Query("SELECT MAX(p.updateDate) FROM PasswordPolicy p")
    Timestamp findLatestUpdateDate();
}
