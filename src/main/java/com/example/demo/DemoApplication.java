package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
class AdditionController {

    private final AdditionService additionService;
    private final SearchService searchService;

    public AdditionController(AdditionService calculatorService, SearchService searchService) {
        this.additionService = calculatorService;
        this.searchService = searchService;
    }

    @GetMapping("/add")
    public AdditionObject calculateSum(@RequestParam int num1, @RequestParam int num2) {
        return additionService.performCalculation(num1, num2);
    }

    @GetMapping("/history")
    public List<AdditionObject> getCalculations() {
            return DemoApplication.getCalculations();
    }

    @GetMapping("/history/search")
    public List<AdditionObject> searchByAdditive(@RequestParam("number") int additive,
                                                 @RequestParam("increasing") boolean increasing) {
            return searchService.searchByAdditive(additive, increasing);
    }
}

@Service
class SearchService {

    public List<AdditionObject> searchByAdditive(int additive, boolean increasing) {
        synchronized (DemoApplication.getCalculations()) {
            List<AdditionObject> results = DemoApplication.getCalculations().stream()
                    .filter(calculation ->
                            calculation.getNum1() == additive ||
                                    calculation.getNum2() == additive ||
                                    calculation.getSum() == additive)
                    .collect(Collectors.toList());

            Comparator<AdditionObject> comparator = Comparator.comparingInt(AdditionObject::getSum);
            if (!increasing) {
                comparator = comparator.reversed();
            }
            results.sort(comparator);

            return results;
        }
    }
}

@Service
class AdditionService {

    public AdditionObject performCalculation(int num1, int num2) {
        if (num1 < 0 || num2 < 0 || num1 > 100 || num2 > 100) {
            System.out.println("Sisesta väärtused vahemikus 0-100!");
            return null;
        } else {
            int sum = num1 + num2;
            AdditionObject result = new AdditionObject(num1, num2, sum);
            DemoApplication.addCalculation(result);
            return result;
        }
    }
}

class AdditionObject {

    private int num1;
    private int num2;
    private int sum;

    public AdditionObject(int num1, int num2, int sum) {
        this.num1 = num1;
        this.num2 = num2;
        this.sum = sum;
    }

    public int getNum1() {
        return num1;
    }


    public int getNum2() {
        return num2;
    }


    public int getSum() {
        return sum;
    }

}

@SpringBootApplication
public class DemoApplication {

    private static final List<AdditionObject> calculations = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    public static List<AdditionObject> getCalculations(){
        synchronized (calculations){
            return new ArrayList<>(calculations);
        }
    }

    public static void addCalculation(AdditionObject calculation){
        synchronized (calculations){
            calculations.add(calculation);
        }
    }

}
