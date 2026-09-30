package com.itszuvalex.technolich.api.storage;

import com.itszuvalex.technolich.api.wrappers.WrapperEnergyHandlerIBattery;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnergyAdapterTest {
    @Test
    void WrapperEnergyHandler_AbortedTransactionRollsBack() {
        var battery = new PowerBattery(100);
        var handler = new WrapperEnergyHandlerIBattery(battery);
        // Act
        try (var tx = Transaction.openRoot()) {
            Assertions.assertEquals(30, handler.insert(30, tx));
            Assertions.assertEquals(30, battery.storage());
        }
        // Assert
        Assertions.assertEquals(0, battery.storage());
    }

    @Test
    void WrapperEnergyHandler_CommittedTransactionPersistsAndClamps() {
        var battery = new PowerBattery(100);
        var handler = new WrapperEnergyHandlerIBattery(battery);
        // Act
        try (var tx = Transaction.openRoot()) {
            Assertions.assertEquals(100, handler.insert(150, tx));
            Assertions.assertEquals(40, handler.extract(40, tx));
            tx.commit();
        }
        // Assert
        Assertions.assertEquals(60, battery.storage());
        Assertions.assertEquals(60, handler.getAmountAsLong());
        Assertions.assertEquals(100, handler.getCapacityAsLong());
    }

    @Test
    void WrapperEnergyHandler_TruncatesFractionalEnergy() {
        var battery = new PowerBattery(100);
        battery.setStorage(10.75);
        var handler = new WrapperEnergyHandlerIBattery(battery);
        // Act
        int extracted;
        try (var tx = Transaction.openRoot()) {
            extracted = handler.extract(50, tx);
            tx.commit();
        }
        // Assert
        Assertions.assertEquals(10, extracted);
        Assertions.assertEquals(0.75, battery.storage(), 1e-9);
    }

    @Test
    void BatteryEnergyHandler_FillAndDrainThroughHandler() {
        var battery = new BatteryEnergyHandler(new SimpleEnergyHandler(100));
        // Act & Assert
        Assertions.assertEquals(100, battery.maxStorage());
        Assertions.assertEquals(80, battery.fill(80.9));
        Assertions.assertEquals(20, battery.room());
        Assertions.assertEquals(20, battery.fill(50));
        Assertions.assertEquals(30, battery.drain(30));
        battery.setStorage(10);
        Assertions.assertEquals(10, battery.storage());
    }
}
