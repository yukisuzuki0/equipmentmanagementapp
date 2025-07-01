package com.example.equipmentmanagement.service;

import com.example.equipmentmanagement.entity.Equipment;
import com.example.equipmentmanagement.entity.EquipmentLifespan;
import com.example.equipmentmanagement.repository.EquipmentLifespanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@Service
public class DepreciationService {

    @Autowired
    private EquipmentLifespanRepository lifespanRepository;

    public double calculateAnnualDepreciation(Equipment equipment) {
        int lifespan = getLifespanYears(equipment);
        if (lifespan <= 0) return 0;
        return equipment.getCost() / lifespan;
    }

    public double calculateAccumulatedDepreciation(Equipment equipment, LocalDate today) {
        int lifespan = getLifespanYears(equipment);
        if (lifespan <= 0) return 0;

        int yearsUsed = Period.between(equipment.getPurchaseDate(), today).getYears();
        yearsUsed = Math.min(yearsUsed, lifespan);

        return calculateAnnualDepreciation(equipment) * yearsUsed;
    }

    public double calculateBookValue(Equipment equipment, LocalDate today) {
        double accumulated = calculateAccumulatedDepreciation(equipment, today);
        double bookValue = equipment.getCost() - accumulated;
        return bookValue < 0 ? 0 : bookValue;
    }

    public int getLifespanYears(Equipment equipment) {
        Optional<EquipmentLifespan> lifespanOpt = lifespanRepository
                .findByCategoryCodeAndItemCode(equipment.getCategoryCode(), equipment.getItemCode());
        return lifespanOpt.map(EquipmentLifespan::getLifespanYears).orElse(0);
    }
}
