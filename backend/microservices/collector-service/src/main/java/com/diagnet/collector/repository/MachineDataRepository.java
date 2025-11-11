package com.diagnet.collector.repository;

import com.diagnet.collector.model.MachineData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for MachineData database operations
 * 
 * WHY THIS IS AN INTERFACE (not a class):
 * - Spring Data JPA automatically implements this at runtime
 * - You write the method signature, Spring generates the SQL!
 * - No need to write boilerplate database code
 * 
 * EXTENDS JpaRepository:
 * - Provides built-in methods: save(), findAll(), findById(), delete(), etc.
 * - <MachineData, Long> means: Entity type is MachineData, ID type is Long
 * 
 * METHOD NAMING CONVENTION:
 * - findBy[FieldName]: Spring generates SELECT query automatically
 * - Example: findByMachineId → SELECT * FROM machine_data WHERE machine_id = ?
 * 
 * CUSTOM QUERIES:
 * - @Query: Write your own SQL/JPQL when method naming isn't enough
 * - Useful for complex queries, joins, aggregations
 */
@Repository
public interface MachineDataRepository extends JpaRepository<MachineData, Long> {

    /**
     * Find all data for a specific machine
     * 
     * Spring automatically generates:
     * SELECT * FROM machine_data WHERE machine_id = ? ORDER BY timestamp DESC
     * 
     * @param machineId Machine identifier (e.g., "M001")
     * @return List of all readings for that machine, newest first
     */
    List<MachineData> findByMachineIdOrderByTimestampDesc(String machineId);

    /**
     * Find recent data for a machine (last N hours)
     * 
     * @param machineId Machine identifier
     * @param startTime Earliest timestamp to include
     * @return List of readings after startTime
     */
    List<MachineData> findByMachineIdAndTimestampAfterOrderByTimestampDesc(
            String machineId, 
            LocalDateTime startTime
    );

    /**
     * Find all data in a time range (for all machines)
     * 
     * Useful for:
     * - "Show me all data from last 24 hours"
     * - "Export data for specific date range"
     * 
     * @param startTime Start of time range
     * @param endTime End of time range
     * @return All readings in that time range
     */
    List<MachineData> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startTime, 
            LocalDateTime endTime
    );

    /**
     * Find data by status (e.g., all machines in "error" state)
     * 
     * Useful for:
     * - Dashboard showing all errored machines
     * - Alert system
     * 
     * @param status Status to filter by ("running", "idle", "error", "maintenance")
     * @return All readings with that status
     */
    List<MachineData> findByStatusOrderByTimestampDesc(String status);

    /**
     * Get recent N records (for dashboard "latest readings" view)
     * 
     * WHY @Query:
     * - Method naming can't express "LIMIT N"
     * - Need custom JPQL query
     * 
     * JPQL vs SQL:
     * - JPQL uses entity/field names (MachineData, timestamp)
     * - SQL uses table/column names (machine_data, timestamp)
     * 
     * @param limit Number of recent records to return
     * @return Most recent N readings across all machines
     */
    @Query("SELECT m FROM MachineData m ORDER BY m.timestamp DESC LIMIT :limit")
    List<MachineData> findRecentData(@Param("limit") int limit);

    /**
     * Count readings for a specific machine
     * 
     * Useful for:
     * - Statistics: "How many readings do we have?"
     * - Monitoring: "Is data still coming in?"
     * 
     * @param machineId Machine identifier
     * @return Total number of readings for that machine
     */
    long countByMachineId(String machineId);

    /**
     * Find machines with high temperature (potential issues)
     * 
     * WHY THIS IS USEFUL:
     * - Proactive monitoring
     * - Find machines that need attention
     * - Feed data to analyzer service
     * 
     * @param minTemperature Minimum temperature threshold (e.g., 100°C)
     * @param since Only check recent data (e.g., last hour)
     * @return Readings where temperature exceeds threshold
     */
    @Query("SELECT m FROM MachineData m WHERE m.temperature > :minTemp AND m.timestamp > :since ORDER BY m.temperature DESC")
    List<MachineData> findHighTemperatureReadings(
            @Param("minTemp") Double minTemperature,
            @Param("since") LocalDateTime since
    );

    /**
     * Find machines with high vibration (potential mechanical issues)
     * 
     * @param minVibration Minimum vibration threshold (e.g., 0.8)
     * @param since Only check recent data
     * @return Readings where vibration exceeds threshold
     */
    @Query("SELECT m FROM MachineData m WHERE m.vibration > :minVibration AND m.timestamp > :since ORDER BY m.vibration DESC")
    List<MachineData> findHighVibrationReadings(
            @Param("minVibration") Double minVibration,
            @Param("since") LocalDateTime since
    );

    /**
     * Get average temperature for a machine in a time period
     * 
     * AGGREGATE QUERY:
     * - AVG() function calculates average
     * - Returns a single number, not a list of entities
     * 
     * @param machineId Machine to analyze
     * @param startTime Start of time period
     * @param endTime End of time period
     * @return Average temperature, or null if no data
     */
    @Query("SELECT AVG(m.temperature) FROM MachineData m WHERE m.machineId = :machineId AND m.timestamp BETWEEN :start AND :end")
    Double getAverageTemperature(
            @Param("machineId") String machineId,
            @Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime
    );

    /**
     * Delete old data (data retention policy)
     * 
     * WHY:
     * - Don't store data forever (costs money, slows queries)
     * - Example: Delete data older than 90 days
     * 
     * WARNING: This permanently deletes data!
     * 
     * @param before Delete all data before this timestamp
     * @return Number of records deleted
     */
    long deleteByTimestampBefore(LocalDateTime before);
}
