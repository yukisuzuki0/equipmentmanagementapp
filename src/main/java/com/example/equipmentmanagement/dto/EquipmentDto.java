package com.example.equipmentmanagement.dto;

import java.time.LocalDate;

public class EquipmentDto {
    private Integer id;
    private String managementNumber;
    private String categoryCode;
    private String itemCode;
    private String name;
    private Double cost;
    private LocalDate purchaseDate;
    private Integer quantity;
    private String locationCode;
    private Boolean isDisposed;

    private Integer lifespanYears;
    private Integer elapsedYears;
    private Double annualDepreciation;
    private Double bookValue;

    // 追加項目
    private String modelNumber;          // 型番
    private String manufacturer;         // メーカー
    private String specification;        // 仕様
    private String depreciationStatus;   // 減価償却状況（例：「終了」）
    private Boolean isBroken;            // 使用不能
    private Boolean isAvailableForLoan;  // 貸出可能
    private String locationLabel;        // 設置場所表示用ラベル
    private LocalDate usageDeadline;     // 使用期限

    // 既存のgetter/setter省略しません

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getManagementNumber() { return managementNumber; }
    public void setManagementNumber(String managementNumber) { this.managementNumber = managementNumber; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public Boolean getIsDisposed() { return isDisposed; }
    public void setIsDisposed(Boolean isDisposed) { this.isDisposed = isDisposed; }

    public Integer getLifespanYears() { return lifespanYears; }
    public void setLifespanYears(Integer lifespanYears) { this.lifespanYears = lifespanYears; }

    public Integer getElapsedYears() { return elapsedYears; }
    public void setElapsedYears(Integer elapsedYears) { this.elapsedYears = elapsedYears; }

    public Double getAnnualDepreciation() { return annualDepreciation; }
    public void setAnnualDepreciation(Double annualDepreciation) { this.annualDepreciation = annualDepreciation; }

    public Double getBookValue() { return bookValue; }
    public void setBookValue(Double bookValue) { this.bookValue = bookValue; }

    // 追加分のgetter/setter

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }

    public String getDepreciationStatus() { return depreciationStatus; }
    public void setDepreciationStatus(String depreciationStatus) { this.depreciationStatus = depreciationStatus; }

    public Boolean getIsBroken() { return isBroken; }
    public void setIsBroken(Boolean isBroken) { this.isBroken = isBroken; }

    public Boolean getIsAvailableForLoan() { return isAvailableForLoan; }
    public void setIsAvailableForLoan(Boolean isAvailableForLoan) { this.isAvailableForLoan = isAvailableForLoan; }

    public String getLocationLabel() { return locationLabel; }
    public void setLocationLabel(String locationLabel) { this.locationLabel = locationLabel; }

    public LocalDate getUsageDeadline() { return usageDeadline; }
    public void setUsageDeadline(LocalDate usageDeadline) { this.usageDeadline = usageDeadline; }
}
