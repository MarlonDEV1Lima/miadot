// OnAnimalClickListener.java
package com.company.miadot.activities; // Ou com.company.miadot.interfaces, se preferir

import com.company.miadot.model.Animal;
import android.util.Log;

public interface OnAnimalClickListener {
    void onAnimalClick(Animal animal);
}