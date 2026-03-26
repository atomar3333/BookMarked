package com.example.demo.repository;

import com.example.demo.entity.Activity;
import com.example.demo.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Activity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(Long userId, ActivityType activityType);

    List<Activity> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);

    List<Activity> findByUserIdInAndActivityTypeOrderByCreatedAtDesc(List<Long> userIds, ActivityType activityType);
}
