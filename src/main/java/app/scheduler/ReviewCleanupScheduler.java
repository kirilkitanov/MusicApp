package app.scheduler;

import app.review.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewCleanupScheduler {

    private final ReviewService reviewService;

    @Autowired
    public ReviewCleanupScheduler(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteReportedReviews() {
        reviewService.deleteAllReportedReviews();

        log.info("All reported reviews were deleted.");
    }

}
