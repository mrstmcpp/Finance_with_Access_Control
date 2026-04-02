package org.mrstm.zorvynfinance.service;

import org.mrstm.zorvynfinance.dto.Dashboard.CategoryTotalResponse;
import org.mrstm.zorvynfinance.dto.Dashboard.DashboardSummaryResponse;
import org.mrstm.zorvynfinance.dto.Dashboard.RecentActivityResponse;
import org.mrstm.zorvynfinance.dto.Dashboard.TrendPointResponse;
import org.mrstm.zorvynfinance.dto.Dashboard.TrendType;
import org.mrstm.zorvynfinance.model.Category;
import org.mrstm.zorvynfinance.model.Transaction;
import org.mrstm.zorvynfinance.model.Type;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final MongoTemplate mongoTemplate;

    public DashboardService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardSummaryResponse getDashboardSummary(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            int recentLimit,
            TrendType trendType,
            int periods
    ) {
        Totals totals = getTotals(userId, startDate, endDate);
        List<CategoryTotalResponse> categoryTotals = getCategoryTotals(userId, startDate, endDate);
        List<RecentActivityResponse> recentActivity = getRecentActivity(userId, recentLimit);
        List<TrendPointResponse> trends = getTrends(userId, startDate, endDate, trendType, periods);

        return DashboardSummaryResponse.builder()
                .totalIncome(totals.totalIncome)
                .totalExpense(totals.totalExpense)
                .netBalance(totals.totalIncome - totals.totalExpense)
                .categoryTotals(categoryTotals)
                .recentActivity(recentActivity)
                .trends(trends)
                .build();
    }

    public List<CategoryTotalResponse> getCategoryTotals(String userId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(buildUserAndDateCriteria(userId, startDate, endDate)),
                Aggregation.group("category")
                        .sum(ConditionalOperators.when(Criteria.where("type").is(Type.INCOME)).thenValueOf("amount").otherwise(0))
                        .as("income")
                        .sum(ConditionalOperators.when(Criteria.where("type").is(Type.EXPENSE)).thenValueOf("amount").otherwise(0))
                        .as("expense"),
                Aggregation.project("income", "expense").and("_id").as("category"),
                Aggregation.sort(Sort.Direction.DESC, "expense")
        );

        AggregationResults<CategoryTotalsAggregateResult> results =
                mongoTemplate.aggregate(aggregation, Transaction.class, CategoryTotalsAggregateResult.class);

        List<CategoryTotalResponse> response = new ArrayList<>();
        for (CategoryTotalsAggregateResult result : results.getMappedResults()) {
            Category category = Category.valueOf(result.category);
            long income = result.income;
            long expense = result.expense;
            response.add(CategoryTotalResponse.builder()
                    .category(category)
                    .income(income)
                    .expense(expense)
                    .net(income - expense)
                    .build());
        }
        return response;
    }

    public List<RecentActivityResponse> getRecentActivity(String userId, int limit) {
        Query query = new Query(Criteria.where("userId").is(userId).and("deleted").ne(true));
        query.with(Sort.by(Sort.Direction.DESC, "date", "createdAt"));
        query.limit(limit);

        List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);

        List<RecentActivityResponse> response = new ArrayList<>();
        for (Transaction transaction : transactions) {
            response.add(RecentActivityResponse.builder()
                    .transactionId(transaction.getId())
                    .type(transaction.getType())
                    .category(transaction.getCategory())
                    .amount(transaction.getAmount())
                    .date(transaction.getDate())
                    .description(transaction.getDescription())
                    .build());
        }

        return response;
    }

    public List<TrendPointResponse> getTrends(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            TrendType trendType,
            int periods
    ) {
        validateDateRange(startDate, endDate);

        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate effectiveStart = startDate != null ? startDate : calculateDefaultStart(effectiveEnd, trendType, periods);

        Query query = new Query(buildUserAndDateCriteria(userId, effectiveStart, effectiveEnd));
        query.with(Sort.by(Sort.Direction.ASC, "date"));

        List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);

        Map<String, TrendAccumulator> buckets = initializeBuckets(effectiveStart, effectiveEnd, trendType);

        for (Transaction transaction : transactions) {
            String bucketKey = toBucketKey(transaction.getDate(), trendType);
            TrendAccumulator accumulator = buckets.get(bucketKey);
            if (accumulator == null) {
                continue;
            }

            if (transaction.getType() == Type.INCOME) {
                accumulator.income += transaction.getAmount();
            } else if (transaction.getType() == Type.EXPENSE) {
                accumulator.expense += transaction.getAmount();
            }
        }

        List<TrendPointResponse> response = new ArrayList<>();
        for (Map.Entry<String, TrendAccumulator> entry : buckets.entrySet()) {
            TrendAccumulator accumulator = entry.getValue();
            response.add(TrendPointResponse.builder()
                    .label(entry.getKey())
                    .income(accumulator.income)
                    .expense(accumulator.expense)
                    .net(accumulator.income - accumulator.expense)
                    .build());
        }

        return response;
    }

    public Totals getTotals(String userId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(buildUserAndDateCriteria(userId, startDate, endDate)),
                Aggregation.group()
                        .sum(ConditionalOperators.when(Criteria.where("type").is(Type.INCOME)).thenValueOf("amount").otherwise(0))
                        .as("totalIncome")
                        .sum(ConditionalOperators.when(Criteria.where("type").is(Type.EXPENSE)).thenValueOf("amount").otherwise(0))
                        .as("totalExpense")
        );

        AggregationResults<Totals> results = mongoTemplate.aggregate(aggregation, Transaction.class, Totals.class);
        Totals totals = results.getUniqueMappedResult();
        return totals == null ? new Totals() : totals;
    }

    private Criteria buildUserAndDateCriteria(String userId, LocalDate startDate, LocalDate endDate) {
        Criteria criteria = Criteria.where("userId").is(userId).and("deleted").ne(true);
        if (startDate != null && endDate != null) {
            criteria.and("date").gte(startDate).lte(endDate);
        }
        return criteria;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException("startDate and endDate must be provided together");
        }

        if (startDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
    }

    private LocalDate calculateDefaultStart(LocalDate effectiveEnd, TrendType trendType, int periods) {
        if (trendType == TrendType.MONTHLY) {
            YearMonth endMonth = YearMonth.from(effectiveEnd);
            return endMonth.minusMonths(periods - 1L).atDay(1);
        }

        LocalDate endWeekStart = effectiveEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return endWeekStart.minusWeeks(periods - 1L);
    }

    private Map<String, TrendAccumulator> initializeBuckets(LocalDate startDate, LocalDate endDate, TrendType trendType) {
        Map<String, TrendAccumulator> buckets = new LinkedHashMap<>();

        if (trendType == TrendType.MONTHLY) {
            YearMonth month = YearMonth.from(startDate);
            YearMonth endMonth = YearMonth.from(endDate);
            while (!month.isAfter(endMonth)) {
                buckets.put(month.toString(), new TrendAccumulator());
                month = month.plusMonths(1);
            }
            return buckets;
        }

        LocalDate cursor = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate limit = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        while (!cursor.isAfter(limit)) {
            buckets.put(cursor.toString(), new TrendAccumulator());
            cursor = cursor.plusWeeks(1);
        }

        return buckets;
    }

    private String toBucketKey(LocalDate date, TrendType trendType) {
        if (trendType == TrendType.MONTHLY) {
            return YearMonth.from(date).toString();
        }

        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
    }

    private static class TrendAccumulator {
        private long income;
        private long expense;
    }

    private static class CategoryTotalsAggregateResult {
        private String category;
        private long income;
        private long expense;
    }

    public static class Totals {
        private long totalIncome;
        private long totalExpense;

        public long getTotalIncome() {
            return totalIncome;
        }

        public long getTotalExpense() {
            return totalExpense;
        }
    }
}

