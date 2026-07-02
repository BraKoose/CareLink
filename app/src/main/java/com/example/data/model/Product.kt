package com.example.data.model

data class Product(
    val id: Int,
    val name: String,
    val priceGhs: Double,
    val description: String,
    val isPrescriptionRequired: Boolean,
    val category: String, // "OTC" or "Prescription"
    val iconName: String  // icon reference helper
)

object StoreCatalog {
    val products = listOf(
        Product(
            id = 1,
            name = "HIV Self-Test Kit (Discreet Pack)",
            priceGhs = 45.00,
            description = "WHO-approved rapid blood-spot testing kit. Get highly accurate results at home in just 15 minutes. Delivered in absolute unmarked outer packaging.",
            isPrescriptionRequired = false,
            category = "OTC Test Kits",
            iconName = "biotech"
        ),
        Product(
            id = 2,
            name = "STI Combo Panel Rapid Test Kit",
            priceGhs = 65.00,
            description = "Discreet multi-pathogen home test strip for Syphilis, Gonorrhea, and Chlamydia. Clear visual readout.",
            isPrescriptionRequired = false,
            category = "OTC Test Kits",
            iconName = "science"
        ),
        Product(
            id = 3,
            name = "Premium Latex Lubricated Condoms (Pack of 12)",
            priceGhs = 15.00,
            description = "Ultra-sensitive, high strength barriers designed for maximum safety, pleasure, and STI prevention.",
            isPrescriptionRequired = false,
            category = "Prevention",
            iconName = "shield"
        ),
        Product(
            id = 4,
            name = "Pregnancy Rapid Test Strip (Pack of 3)",
            priceGhs = 10.00,
            description = "High sensitivity HCG early detection urine test strips. 99% accuracy.",
            isPrescriptionRequired = false,
            category = "Prevention",
            iconName = "check_circle"
        ),
        Product(
            id = 5,
            name = "PrEP (Pre-Exposure Prophylaxis) - 30 Day Supply",
            priceGhs = 120.00,
            description = "Daily medication to reduce risk of HIV acquisition in HIV-negative individuals. Requires a valid doctor's prescription and regular kidney function checks.",
            isPrescriptionRequired = true,
            category = "Prescription medication",
            iconName = "medical_services"
        ),
        Product(
            id = 6,
            name = "PEP (Post-Exposure Prophylaxis) - 28 Day Emergency Kit",
            priceGhs = 180.00,
            description = "Emergency antiretroviral regimen to be started within 72 hours of potential HIV exposure. Requires an urgent verified clinical prescription.",
            isPrescriptionRequired = true,
            category = "Prescription medication",
            iconName = "emergency"
        ),
        Product(
            id = 7,
            name = "ARV Refill (Tenofovir/Lamivudine/Dolutegravir - TLD)",
            priceGhs = 150.00,
            description = "Monthly first-line antiretroviral supply for viral load suppression. Requires standard clinical prescription renewal.",
            isPrescriptionRequired = true,
            category = "Prescription medication",
            iconName = "medication"
        )
    )
}
