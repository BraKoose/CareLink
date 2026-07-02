package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.data.model.StoreCatalog
import com.example.ui.CareLinkViewModel

data class DisguiseOption(val name: String, val price: Double, val description: String, val icon: String)

val disguiseOptions = listOf(
    DisguiseOption("Standard Unbranded Box", 0.0, "Plain cardboard box with zero markings and secure tape", "📦"),
    DisguiseOption("Pizza Box Disguise", 5.0, "Carefully enclosed inside a local pizza box delivery", "🍕"),
    DisguiseOption("Warm Jollof Rice Lunchbox", 12.0, "Fresh warm Jollof Rice with grilled chicken. Fully disguises your package inside an authentic hot food delivery bag", "🍗"),
    DisguiseOption("Nutritious Milo & Oats Pack", 15.0, "Cocoa Milo powder and whole grain oats. Highly recommended for physical strength, recovery, and daily immune support", "🥣"),
    DisguiseOption("Fresh Fruit & Orange Crate", 18.0, "Immune-boosting fresh oranges, bananas, and apples in an organic grocery bag to ensure total privacy", "🍊"),
    DisguiseOption("High-Protein Shake & Nuts", 14.0, "Nutritional high-protein drink paired with vitamin-rich roasted cashews. Perfect for medication adherence", "🥜"),
    DisguiseOption("Supermarket Grocery Bag", 2.0, "Opaque grocery carrier bag with security tie", "🛍️"),
    DisguiseOption("Official Document Mailer", 1.0, "Cardboard mailing packet for letters/bills", "✉️")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: CareLinkViewModel,
    modifier: Modifier = Modifier
) {
    val cart by viewModel.cart.collectAsState()
    val rxProductPending by viewModel.selectedProductForRxUpload.collectAsState()
    val prescriptionPhotoPath by viewModel.prescriptionPhotoPath.collectAsState()

    var showCartDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showOrderConfirmedDialog by remember { mutableStateOf(false) }

    val cartItemCount = cart.values.sum()
    val totalPrice = cart.entries.sumOf { it.key.priceGhs * it.value }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- STORE FRONT HEADER WITH CART BUTTON ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CareLink Storefront",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Discreet wellness products shipped in absolute anonymity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Cart Icon with Badge
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge { Text("$cartItemCount") }
                        }
                    },
                    modifier = Modifier
                        .clickable { showCartDialog = true }
                        .padding(8.dp)
                        .testTag("cart_badge_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "View Cart",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PRODUCT LISTS BY CATEGORY ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Happy storefront banner with image
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.store_banner_1782975793227),
                                contentDescription = "CareLink Wellness Shop",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // A translucent overlay to make it look premium
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f)
                                        )
                                    ))
                            )
                            // Text inside the banner
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "100% Secure & Anonymized",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ghana health guidelines compliant pharmacy",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // CATEGORY: OVER THE COUNTER
                item {
                    CategoryHeader(title = "Over-the-Counter & Self-Tests")
                }

                items(StoreCatalog.products.filter { !it.isPrescriptionRequired }) { product ->
                    ProductItemCard(
                        product = product,
                        onAddToCartClick = {
                            viewModel.addProductToCart(product)
                        }
                    )
                }

                // CATEGORY: PRESCRIPTION MEDS (Rx REQUIRED)
                item {
                    CategoryHeader(title = "Prescription Medications (Rx Required)")
                }

                items(StoreCatalog.products.filter { it.isPrescriptionRequired }) { product ->
                    ProductItemCard(
                        product = product,
                        onAddToCartClick = {
                            // Triggers prescription upload flow
                            viewModel.setProductForRxUpload(product)
                        }
                    )
                }
            }
        }

        // --- CART OVERLAY DIALOG ---
        if (showCartDialog) {
            AlertDialog(
                onDismissRequest = { showCartDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Confidential Shopping Cart", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showCartDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    if (cart.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your cart is empty", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                            items(cart.entries.toList()) { (product, qty) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("GHS ${product.priceGhs} each", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        if (product.isPrescriptionRequired) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lock, contentDescription = "Rx Verified", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Prescription Attached", color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.removeProductFromCart(product) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                        }
                                        Text("$qty", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(
                                            onClick = { viewModel.addProductToCart(product) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                },
                confirmButton = {
                    if (cart.isNotEmpty()) {
                        Button(
                            onClick = {
                                showCartDialog = false
                                showCheckoutDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_button")
                        ) {
                            Text("Proceed to Checkout • GHS $totalPrice")
                        }
                    }
                }
            )
        }

        // --- MANDATORY PRESCRIPTION UPLOAD FLOW OVERLAY ---
        rxProductPending?.let { product ->
            AlertDialog(
                onDismissRequest = { viewModel.setProductForRxUpload(null) },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prescription Required", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(onClick = { viewModel.setProductForRxUpload(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "To purchase ${product.name}, Ghana health safety guidelines require you to upload an authentic photo of your physician's prescription.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        // Upload simulation box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    width = 1.dp,
                                    color = if (prescriptionPhotoPath != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    // simulate upload
                                    viewModel.uploadPrescriptionSimulated()
                                }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (prescriptionPhotoPath == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload Photo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Capture / Upload Prescription Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Max size 10MB (JPG, PNG)", fontSize = 10.sp, color = Color.Gray)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Upload Complete",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Prescription Upload Successful", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text("File Attached: ${prescriptionPhotoPath!!.substringAfterLast("/")}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Helpful instructions
                        Text(
                            text = "*Prescription is reviewed confidentially by an accredited online pharmacist in Ghana before dispatch. Your name is hashed during order verification.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Add prescription product to cart
                            viewModel.addProductToCart(product)
                            viewModel.setProductForRxUpload(null) // clear upload pending state
                        },
                        enabled = prescriptionPhotoPath != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_prescription_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Verify & Add to Cart")
                    }
                }
            )
        }

        // --- CHECKOUT CONFIDENTIAL DELIVERY DETAILS DIALOG ---
        if (showCheckoutDialog) {
            var addressNote by remember { mutableStateOf("") }
            var selectedDisguise by remember { mutableStateOf(disguiseOptions[0]) }
            val totalWithDisguise = totalPrice + selectedDisguise.price

            AlertDialog(
                onDismissRequest = { showCheckoutDialog = false },
                title = { Text("Complete Discreet Delivery", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "To guarantee absolute safety and anonymity, we recommend delivering to a neutral pickup office, a local post drawer, or specifying delivery guidelines without putting your real name.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = addressNote,
                                onValueChange = { addressNote = it },
                                label = { Text("Delivery Note / Pickup Landmark") },
                                placeholder = { Text("e.g. Leave at Accra Post Office Counter #4 or call for landmark") },
                                modifier = Modifier.fillMaxWidth().testTag("address_note_input"),
                                minLines = 2
                            )
                        }

                        item {
                            Text(
                                text = "Select Delivery Disguise Packaging",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(disguiseOptions) { option ->
                            val isSelected = selectedDisguise == option
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDisguise = option }
                                    .testTag("disguise_option_${option.name.replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option.icon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = option.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (option.price == 0.0) "Free" else "+ GHS ${option.price}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (option.price == 0.0) MaterialTheme.colorScheme.secondary else Color.Gray
                                            )
                                        }
                                        Text(
                                            text = option.description,
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Packaging disguise: ${selectedDisguise.name} guaranteed.", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.checkoutCart(selectedDisguise.name, selectedDisguise.price)
                            showCheckoutDialog = false
                            showOrderConfirmedDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_checkout_button")
                    ) {
                        Text("Confirm Order • Total GHS $totalWithDisguise")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("Back to Cart")
                    }
                }
            )
        }

        // --- ORDER PLACED SUCCESS DIALOG ---
        if (showOrderConfirmedDialog) {
            AlertDialog(
                onDismissRequest = { showOrderConfirmedDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp)) },
                title = { Text("Order Placed Successfully", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                text = {
                    Text(
                        text = "Your confidential order has been queued. You can monitor progress inside the 'Discreet Deliveries' tracker on your dashboard. Deliveries inside Accra arrive in 4-6 hours.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showOrderConfirmedDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome")
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.Gray,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun ProductItemCard(
    product: Product,
    onAddToCartClick: () -> Unit
) {
    // Determine the beautiful visual details based on product
    val (emoji, containerBg, labelText) = when {
        product.name.contains("Self-Test", ignoreCase = true) -> Triple("🔬", Color(0xFFE0F7FA), "Home Test")
        product.name.contains("Panel", ignoreCase = true) -> Triple("🧬", Color(0xFFE0F2F1), "Multi Test")
        product.name.contains("Condoms", ignoreCase = true) -> Triple("🛡️", Color(0xFFE8F5E9), "Safety Care")
        product.name.contains("Pregnancy", ignoreCase = true) -> Triple("👶", Color(0xFFFCE4EC), "Family Check")
        product.name.contains("PrEP", ignoreCase = true) -> Triple("💊", Color(0xFFFFF9C4), "Daily Shield")
        product.name.contains("PEP", ignoreCase = true) -> Triple("🚨", Color(0xFFFFEBEE), "Emergency")
        product.name.contains("ARV", ignoreCase = true) -> Triple("🌟", Color(0xFFE8EAF6), "TLD Vital")
        else -> Triple("📦", Color(0xFFF5F5F5), "Wellness")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cheerful, colorful visual representation
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = labelText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details and actions
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "GHS ${product.priceGhs}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rx Required Indicator Badge
                    if (product.isPrescriptionRequired) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Red.copy(alpha = 0.05f))
                                .border(1.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Red, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rx Required", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Over-the-Counter", color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onAddToCartClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_to_cart_${product.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (product.isPrescriptionRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
