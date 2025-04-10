package com.example.demo.Controller;

import com.example.demo.DAO.operations.DishDAO;
import com.example.demo.Entity.Dish;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DishController {
    private DishDAO dishDAO = new DishDAO();

    @GetMapping("/dishes")
    public List<Dish> dish() {
        return dishDAO.getAll();
    }
}
