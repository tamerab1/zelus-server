package com.near_reality.cache_tool.packing.custom

import mgi.types.config.StructDefinitions

object NearRealityCustomStructsPacker {
    @JvmStatic
    fun pack() {
        // Boss structs (bestaand voorbeeld)
        StructDefinitions.get(500).copy(10500).apply {
            this.parameters[689] = "Duke Sucellus"
            this.parameters[690] = 10500
            this.pack()
        }
        StructDefinitions.get(500).copy(10501).apply {
            this.parameters[689] = "Vardorvis"
            this.parameters[690] = 10501
            this.pack()
        }
        StructDefinitions.get(500).copy(10502).apply {
            this.parameters[689] = "Tormented Demons"
            this.parameters[690] = 10502
            this.pack()
        }
        StructDefinitions.get(500).copy(10503).apply {
            this.parameters[689] = "Araxxor"
            this.parameters[690] = 10503
            this.pack()
        }

        // Wilderness categorie zelf
        StructDefinitions.get(474).apply {
            this.parameters[682] = "<col=ff0000>Wilderness</col>"
            this.parameters[690] = 2106   // verwijst naar enum met alle substructs
            this.pack()
        }

        // Novice & Recruit labels bijwerken
        StructDefinitions.get(518).apply {
            this.parameters[689] = "Wilderness Novice"
            this.pack()
        }
        StructDefinitions.get(522).apply {
            this.parameters[689] = "Wilderness Recruit"
            this.pack()
        }

        // Nieuwe Veteran struct
        StructDefinitions.get(518).copy(1283).apply {
            this.parameters[689] = "Wilderness Veteran"
            this.parameters[690] = 3041   // verwijzing naar de items-enum
            this.pack()
        }
    }
}
