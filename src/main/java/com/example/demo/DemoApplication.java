package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
class AdditionController {

    private final AdditionService additionService;
    private final SearchService searchService;


    @Autowired
    public AdditionController(AdditionService calculatorService, SearchService searchService) {
        this.additionService = calculatorService;
        this.searchService = searchService;
    }

    @GetMapping("/add/{num1}/{num2}")
    public AdditionObject calculateSum(@PathVariable int num1, @PathVariable int num2) {
        return additionService.performCalculation(num1, num2);
    }

    @GetMapping("/history")
    public List<AdditionObject> getCalculations() {
        synchronized (DemoApplication.calculations) {
            return DemoApplication.calculations;
        }
    }

    @GetMapping("/history/search")
    public List<AdditionObject> searchByAdditive(@RequestParam("number") int additive,
                                                 @RequestParam("increasing") boolean increasing) {
        synchronized (DemoApplication.calculations) {
            return searchService.searchByAdditive(additive, increasing);
        }
    }

}

@Service
class SearchService {

    public List<AdditionObject> searchByAdditive(int additive, boolean increasing) {
        List<AdditionObject> results = new ArrayList<>();

        synchronized (DemoApplication.calculations) {
            for (AdditionObject calculation : DemoApplication.calculations) {
                if (calculation.getNum1() == additive || calculation.getNum2() == additive || calculation.getSum() == additive) {
                    results.add(calculation);
                }
            }
        }

        Comparator<AdditionObject> comparator = Comparator.comparingInt(AdditionObject::getSum);
        if (!increasing) {
            comparator = comparator.reversed();
        }
        results.sort(comparator);

        return results;
    }
}

@Service
class AdditionService {

    public synchronized AdditionObject performCalculation(int num1, int num2) {
        if (num1 < 0 || num2 < 0 || num1 > 100 || num2 > 100) {
            System.out.println("Sisesta väärtused vahemikus 0-100!");
            return null;
        } else {
            int sum = num1 + num2;
            AdditionObject result = new AdditionObject(num1, num2, sum);
            DemoApplication.calculations.add(result);
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

    public void setNum1(int num1) {
        this.num1 = num1;
    }

    public int getNum2() {
        return num2;
    }

    public void setNum2(int num2) {
        this.num2 = num2;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }
}

@SpringBootApplication
public class DemoApplication {

    static final List<AdditionObject> calculations = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
