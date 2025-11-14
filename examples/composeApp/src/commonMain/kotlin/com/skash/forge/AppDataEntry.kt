package com.skash.forge

import com.skash.forge.datastore.DataEntry

object AppDataEntry{

    val Count = DataEntry.int(
        "count",
        defaultValue = 0
    )

}