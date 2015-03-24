package com.davidmato.pv168fuelapp;


import  com.davidmato.pv168fuelapp.entity.FillUp;
;
import java.util.List;

public interface FillUpManager {
	void createFillUp(FillUp fillUp);
	void updateFillUp(FillUp fillUp);
	void deleteFillUp(FillUp fillUp);
	FillUp findFillUpById(Long id);
	List<FillUp> findAllFillUps();
	
}
