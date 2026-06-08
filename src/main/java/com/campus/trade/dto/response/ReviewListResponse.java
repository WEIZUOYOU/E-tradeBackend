package com.campus.trade.dto.response;

import com.campus.trade.entity.Review;
import lombok.Data;

import java.util.List;

@Data
public class ReviewListResponse {
    private List<Review> reviews;
    private Double averageRating;
    private Integer reviewCount;
}