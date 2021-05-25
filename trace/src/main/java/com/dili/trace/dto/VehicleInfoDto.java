package com.dili.trace.dto;

import com.dili.customer.sdk.domain.VehicleInfo;

public class VehicleInfoDto {
    /**
     * 车牌
     */
    private String vehiclePlate;
    /**
     * 车型ID
     */
    private Long vehicleType;

    /**
     * 车型名称
     */
    private String vehicleTypeName;

    /**
     * build对象
     *
     * @param v
     * @param carType
     * @return
     */
    public static VehicleInfoDto build(VehicleInfo v, String carType) {
        VehicleInfoDto vehicleInfoDto = new VehicleInfoDto();
        vehicleInfoDto.setVehiclePlate(v.getRegistrationNumber());
        vehicleInfoDto.setVehicleType(v.getTypeNumber());
        vehicleInfoDto.setVehicleTypeName(carType);
        return vehicleInfoDto;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public Long getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(Long vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleTypeName() {
        return vehicleTypeName;
    }

    public void setVehicleTypeName(String vehicleTypeName) {
        this.vehicleTypeName = vehicleTypeName;
    }

}
