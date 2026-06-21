// QuickBite Customer Interactive Cart & Payment Controller

// Cache DOM elements
var DOM = {
  themeToggle: document.getElementById('theme-toggle'),
  cartToggle: document.getElementById('cart-toggle'),
  cartBadge: document.getElementById('cart-badge-count'),
  cartDrawer: document.getElementById('cart-drawer'),
  cartClose: document.getElementById('cart-close'),
  cartItemsList: document.getElementById('cart-items-list'),
  cartEmptyState: document.getElementById('cart-empty-state'),
  subtotalVal: document.getElementById('subtotal-val'),
  deliveryVal: document.getElementById('delivery-val'),
  taxVal: document.getElementById('tax-val'),
  totalVal: document.getElementById('total-val'),
  checkoutBtn: document.getElementById('checkout-btn'),
  
  // Checkout Modal
  checkoutOverlay: document.getElementById('checkout-overlay'),
  checkoutClose: document.getElementById('checkout-close'),
  checkoutForm: document.getElementById('checkout-form'),
  creditCard: document.getElementById('credit-card'),
  ccNumberInput: document.getElementById('cc-number'),
  ccNameInput: document.getElementById('cc-name'),
  ccExpiryInput: document.getElementById('cc-expiry'),
  ccCvvInput: document.getElementById('cc-cvv'),
  ccNumberDisplay: document.getElementById('cc-display-number'),
  ccNameDisplay: document.getElementById('cc-display-name'),
  ccExpiryDisplay: document.getElementById('cc-display-expiry'),
  ccCvvDisplay: document.getElementById('cc-display-cvv')
};

var cartState = {
  items: [],
  subtotal: 0,
  totalQty: 0
};

// Initialize listeners
document.addEventListener('DOMContentLoaded', function() {
  initTheme();
  setupEventListeners();
  loadCart();
});

// Theme Logic
function initTheme() {
  var savedTheme = localStorage.getItem('qb-theme') || 'dark';
  document.documentElement.setAttribute('data-theme', savedTheme);
  if (DOM.themeToggle) {
    DOM.themeToggle.innerHTML = savedTheme === 'dark' ? '☀️' : '🌙';
  }
}

function toggleTheme() {
  var current = document.documentElement.getAttribute('data-theme') || 'dark';
  var next = current === 'light' ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem('qb-theme', next);
  if (DOM.themeToggle) {
    DOM.themeToggle.innerHTML = next === 'dark' ? '☀️' : '🌙';
  }
}

// Fetch Cart details from Servlet
function loadCart() {
  fetch('cart')
    .then(function(res) { return res.json(); })
    .then(function(data) {
      if (data.status === 'success') {
        cartState.items = data.items;
        cartState.subtotal = data.subtotal;
        cartState.totalQty = data.totalQuantity;
        updateCartUI();
      }
    })
    .catch(function(err) { console.error('Error loading cart:', err); });
}

function addToCart(menuItemId, quantity) {
  fetch('cart?action=add&menuItemId=' + menuItemId + '&quantity=' + quantity, { method: 'POST' })
    .then(function(res) { return res.json(); })
    .then(function(data) {
      if (data.status === 'success') {
        cartState.items = data.items;
        cartState.subtotal = data.subtotal;
        cartState.totalQty = data.totalQuantity;
        updateCartUI();
        
        // Bounce cart button
        if (DOM.cartToggle) {
          DOM.cartToggle.style.transform = 'scale(1.25)';
          setTimeout(function() { DOM.cartToggle.style.transform = ''; }, 200);
        }
      }
    })
    .catch(function(err) { console.error('Error adding to cart:', err); });
}

function updateCartQty(menuItemId, quantity) {
  fetch('cart?action=update&menuItemId=' + menuItemId + '&quantity=' + quantity, { method: 'POST' })
    .then(function(res) { return res.json(); })
    .then(function(data) {
      if (data.status === 'success') {
        cartState.items = data.items;
        cartState.subtotal = data.subtotal;
        cartState.totalQty = data.totalQuantity;
        updateCartUI();
      }
    })
    .catch(function(err) { console.error('Error updating quantity:', err); });
}

function removeCartItem(menuItemId) {
  fetch('cart?action=remove&menuItemId=' + menuItemId, { method: 'POST' })
    .then(function(res) { return res.json(); })
    .then(function(data) {
      if (data.status === 'success') {
        cartState.items = data.items;
        cartState.subtotal = data.subtotal;
        cartState.totalQty = data.totalQuantity;
        updateCartUI();
      }
    })
    .catch(function(err) { console.error('Error removing item:', err); });
}

function updateCartUI() {
  if (!DOM.cartBadge) return;

  // Badge count
  DOM.cartBadge.textContent = cartState.totalQty;
  DOM.cartBadge.style.display = cartState.totalQty > 0 ? 'flex' : 'none';

  if (cartState.items.length === 0) {
    DOM.cartItemsList.style.display = 'none';
    DOM.cartEmptyState.style.display = 'flex';
    DOM.subtotalVal.textContent = '₹0.00';
    DOM.taxVal.textContent = '₹0.00';
    DOM.totalVal.textContent = '₹0.00';
    DOM.checkoutBtn.disabled = true;
    return;
  }

  DOM.cartItemsList.style.display = 'flex';
  DOM.cartEmptyState.style.display = 'none';
  DOM.checkoutBtn.disabled = false;

  DOM.cartItemsList.innerHTML = '';
  cartState.items.forEach(function(item) {
    var itemNode = document.createElement('div');
    itemNode.className = 'cart-item';
    itemNode.innerHTML = 
      '<div class="cart-item-img">' +
      '  <img src="https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100&auto=format&fit=crop&q=60" alt="' + item.name + '">' +
      '</div>' +
      '<div class="cart-item-content">' +
      '  <h4 class="cart-item-title">' + item.name + '</h4>' +
      '  <div class="cart-item-footer">' +
      '    <span class="cart-item-price">₹' + item.totalPrice.toFixed(2) + '</span>' +
      '    <div class="cart-qty-selector">' +
      '      <button class="cart-qty-btn dec-qty">-</button>' +
      '      <span class="cart-qty-val">' + item.quantity + '</span>' +
      '      <button class="cart-qty-btn inc-qty">+</button>' +
      '    </div>' +
      '  </div>' +
      '</div>' +
      '<button class="cart-item-remove">✕</button>';

    // Bind triggers
    itemNode.querySelector('.dec-qty').onclick = function() { updateCartQty(item.menuItemId, item.quantity - 1); };
    itemNode.querySelector('.inc-qty').onclick = function() { updateCartQty(item.menuItemId, item.quantity + 1); };
    itemNode.querySelector('.cart-item-remove').onclick = function() { removeCartItem(item.menuItemId); };

    DOM.cartItemsList.appendChild(itemNode);
  });

  // Calculate pricing (Rupees)
  var deliveryFee = 30.00; // Rs. 30 flat delivery
  var tax = cartState.subtotal * 0.05; // 5% GST
  var grandTotal = cartState.subtotal + deliveryFee + tax;

  DOM.subtotalVal.textContent = '₹' + cartState.subtotal.toFixed(2);
  DOM.deliveryVal.textContent = '₹' + deliveryFee.toFixed(2);
  DOM.taxVal.textContent = '₹' + tax.toFixed(2);
  DOM.totalVal.textContent = '₹' + grandTotal.toFixed(2);

  // Update dynamic UPI QR Code
  var upiQrImg = document.getElementById('upi-qr-code');
  if (upiQrImg) {
    upiQrImg.src = 'https://api.qrserver.com/v1/create-qr-code/?size=130x130&data=upi://pay?pa=quickbite@bank%26pn=QuickBite%26am=' + grandTotal.toFixed(2) + '%26cu=INR';
  }
}

function handleCardInputFormatting(e) {
  var target = e.target;
  var val = target.value.replace(/\D/g, '');
  if (target.id === 'cc-number') {
    val = val.substring(0, 16);
    var formatted = '';
    var match = val.match(/.{1,4}/g);
    if (match) {
      formatted = match.join(' ');
    }
    target.value = formatted;
    DOM.ccNumberDisplay.textContent = formatted || '•••• •••• •••• ••••';
  } else if (target.id === 'cc-expiry') {
    val = val.substring(0, 4);
    var formattedExpiry = val.length >= 2 ? val.substring(0, 2) + '/' + val.substring(2) : val;
    target.value = formattedExpiry;
    DOM.ccExpiryDisplay.textContent = formattedExpiry || 'MM/YY';
  }
}

function animateFlyToCart(menuItemId) {
  var card = document.querySelector('.dish-card[data-id="' + menuItemId + '"]');
  var cartIcon = document.getElementById('cart-toggle');
  if (!card || !cartIcon) return;
  
  var img = card.querySelector('.card-img-container img');
  if (!img) return;
  
  // Clone image
  var clone = img.cloneNode();
  var rect = img.getBoundingClientRect();
  var cartRect = cartIcon.getBoundingClientRect();
  
  // Initial styles matching source image position
  clone.style.position = 'fixed';
  clone.style.top = rect.top + 'px';
  clone.style.left = rect.left + 'px';
  clone.style.width = rect.width + 'px';
  clone.style.height = rect.height + 'px';
  clone.style.borderRadius = '18px';
  clone.style.zIndex = '9999';
  clone.style.pointerEvents = 'none';
  clone.style.transition = 'all 0.85s cubic-bezier(0.19, 1, 0.22, 1)';
  
  document.body.appendChild(clone);
  
  // Force reflow
  clone.getBoundingClientRect();
  
  // Animate to target cart icon
  clone.style.top = (cartRect.top + 5) + 'px';
  clone.style.left = (cartRect.left + 5) + 'px';
  clone.style.width = '30px';
  clone.style.height = '30px';
  clone.style.opacity = '0.3';
  clone.style.transform = 'rotate(720deg) scale(0.5)';
  
  setTimeout(function() {
    clone.remove();
    cartIcon.classList.add('bounce-active');
    setTimeout(function() { cartIcon.classList.remove('bounce-active'); }, 500);
  }, 850);
}

function setupEventListeners() {
  if (DOM.themeToggle) DOM.themeToggle.onclick = toggleTheme;

  // Cart Drawer open/close
  if (DOM.cartToggle) {
    DOM.cartToggle.onclick = function() { DOM.cartDrawer.classList.add('active'); };
  }
  if (DOM.cartClose) {
    DOM.cartClose.onclick = function() { DOM.cartDrawer.classList.remove('active'); };
  }

  // Hook customization submit button in customer-dashboard.jsp
  var modalAddSubmit = document.getElementById('modal-add-to-cart-submit');
  if (modalAddSubmit) {
    modalAddSubmit.onclick = function() {
      if (selectedMenuItemId) {
        animateFlyToCart(selectedMenuItemId);
        addToCart(selectedMenuItemId, selectedQty);
        document.getElementById('customize-overlay').classList.remove('active');
      }
    };
  }

  // Checkout modal toggles
  if (DOM.checkoutBtn) {
    DOM.checkoutBtn.onclick = function() {
      DOM.cartDrawer.classList.remove('active');
      DOM.checkoutOverlay.classList.add('active');
    };
  }
  if (DOM.checkoutClose) {
    DOM.checkoutClose.onclick = function() { DOM.checkoutOverlay.classList.remove('active'); };
  }

  // Credit Card formatting & focus events
  if (DOM.ccNumberInput) {
    DOM.ccNumberInput.oninput = handleCardInputFormatting;
    DOM.ccExpiryInput.oninput = handleCardInputFormatting;
    DOM.ccNameInput.oninput = function(e) {
      DOM.ccNameDisplay.textContent = e.target.value.toUpperCase() || 'CARDHOLDER NAME';
    };
    DOM.ccCvvInput.oninput = function(e) {
      var val = e.target.value.replace(/\D/g, '').substring(0, 3);
      e.target.value = val;
      DOM.ccCvvDisplay.textContent = val || '•••';
    };
    DOM.ccCvvInput.onfocus = function() { DOM.creditCard.classList.add('flipped'); };
    DOM.ccCvvInput.onblur = function() { DOM.creditCard.classList.remove('flipped'); };
  }

  // Form submission handler
  if (DOM.checkoutForm) {
    DOM.checkoutForm.onsubmit = function(e) {
      e.preventDefault();
      
      // Fetch details of first item to get restaurant reference
      if (cartState.items.length === 0) return;
      
      // Query menu_item details from card list
      var firstItemId = cartState.items[0].menuItemId;
      var card = document.querySelector('.dish-card[data-id="' + firstItemId + '"]');
      var restaurantId = card ? card.dataset.restaurantId : 1;
      
      var deliveryFee = 30.00;
      var tax = cartState.subtotal * 0.05;
      var grandTotal = cartState.subtotal + deliveryFee + tax;
      
      // Get address and active payment method
      var address = document.getElementById('del-address').value;
      
      var activeMethod = 'card';
      var upiFields = document.getElementById('upi-payment-fields');
      var podFields = document.getElementById('pod-payment-fields');
      if (upiFields && upiFields.style.display === 'block') {
        activeMethod = 'upi';
      } else if (podFields && podFields.style.display === 'block') {
        activeMethod = 'pod';
      }
      
      // Save details to localStorage
      localStorage.setItem('qb_last_address', address);
      localStorage.setItem('qb_last_payment_method', activeMethod);
      
      var submitBtn = DOM.checkoutForm.querySelector('button[type="submit"]');
      var originalBtnText = submitBtn ? submitBtn.innerHTML : 'Pay & Place Order';
      
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-btn" style="display:inline-block; width:12px; height:12px; border:2px solid #fff; border-radius:50%; border-top-color:transparent; animation:rotate-ring 0.8s linear infinite; margin-right:8px; vertical-align:middle;"></span>Authorizing Card...';
      }
      
      if (activeMethod === 'card') {
        var ccNum = DOM.ccNumberInput.value;
        var ccName = DOM.ccNameInput.value;
        var ccExp = DOM.ccExpiryInput.value;
        var ccCvv = DOM.ccCvvInput.value;
        
        var bodyParams = 'restaurantId=' + encodeURIComponent(restaurantId) +
                         '&totalPrice=' + encodeURIComponent(grandTotal) +
                         '&cardNumber=' + encodeURIComponent(ccNum) +
                         '&cardHolder=' + encodeURIComponent(ccName) +
                         '&cardExpiry=' + encodeURIComponent(ccExp) +
                         '&cardCvv=' + encodeURIComponent(ccCvv);
                         
        fetch('checkout/stripe', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: bodyParams
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
          if (data.status === 'success') {
            DOM.checkoutOverlay.classList.remove('active');
            window.location.href = 'order-tracking.jsp?orderId=' + data.orderId;
          } else {
            alert('Payment authorization failed: ' + data.message);
            if (submitBtn) {
              submitBtn.disabled = false;
              submitBtn.innerHTML = originalBtnText;
            }
          }
        })
        .catch(function(err) {
          console.error('Stripe payment failed:', err);
          alert('Failed to authorize payment via mock Stripe API.');
          if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalBtnText;
          }
        });
      } else {
        // If Pay on Delivery, show confirmation prompt
        if (activeMethod === 'pod') {
          var confirmed = confirm('🛵 Confirm Pay on Delivery Order\n\nYour order total is ₹' + grandTotal.toFixed(2) + '. Are you sure you want to place this order using Cash/UPI on Delivery? You will pay the rider at your door.');
          if (!confirmed) {
            if (submitBtn) {
              submitBtn.disabled = false;
              submitBtn.innerHTML = originalBtnText;
            }
            return;
          }
        }
        
        // Place Order via POST REST
        fetch('customer?action=placeOrder&restaurantId=' + restaurantId + '&totalPrice=' + grandTotal, { method: 'POST' })
          .then(function(res) { return res.json(); })
          .then(function(data) {
            if (data.status === 'success') {
              DOM.checkoutOverlay.classList.remove('active');
              window.location.href = 'order-tracking.jsp?orderId=' + data.orderId;
            } else {
              alert('Failed to place order: ' + data.message);
              if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalBtnText;
              }
            }
          })
          .catch(function(err) {
            console.error('Order placing failed:', err);
            if (submitBtn) {
              submitBtn.disabled = false;
              submitBtn.innerHTML = originalBtnText;
            }
          });
      }
    };
  }
}

// Payment Method Switcher
window.switchPayMethod = function(method) {
  // Update tab buttons
  document.querySelectorAll('.pay-method-btn').forEach(function(btn) {
    btn.classList.remove('active');
  });
  
  // Set current button to active
  var clickedBtn = event ? event.currentTarget : null;
  if (clickedBtn) {
    clickedBtn.classList.add('active');
  } else {
    var btnMap = { card: 0, upi: 1, pod: 2 };
    var btns = document.querySelectorAll('.pay-method-btn');
    if (btns[btnMap[method]]) btns[btnMap[method]].classList.add('active');
  }
  
  // Show/Hide sections
  var cardFields = document.getElementById('card-payment-fields');
  var upiFields = document.getElementById('upi-payment-fields');
  var podFields = document.getElementById('pod-payment-fields');
  var cardContainerVisual = document.querySelector('.card-container');
  
  // Hide all first
  if (cardFields) cardFields.style.display = 'none';
  if (upiFields) upiFields.style.display = 'none';
  if (podFields) podFields.style.display = 'none';
  
  // Reset required attributes on card inputs so form validation works for other methods!
  var ccNum = document.getElementById('cc-number');
  var ccName = document.getElementById('cc-name');
  var ccExpiry = document.getElementById('cc-expiry');
  var ccCvv = document.getElementById('cc-cvv');
  var upiId = document.getElementById('upi-id');
  
  if (ccNum) ccNum.required = false;
  if (ccName) ccName.required = false;
  if (ccExpiry) ccExpiry.required = false;
  if (ccCvv) ccCvv.required = false;
  if (upiId) upiId.required = false;
  
  if (method === 'card') {
    if (cardFields) cardFields.style.display = 'block';
    if (cardContainerVisual) {
      cardContainerVisual.style.opacity = '1';
      cardContainerVisual.style.pointerEvents = 'auto';
    }
    if (ccNum) ccNum.required = true;
    if (ccName) ccName.required = true;
    if (ccExpiry) ccExpiry.required = true;
    if (ccCvv) ccCvv.required = true;
  } else if (method === 'upi') {
    if (upiFields) upiFields.style.display = 'block';
    if (cardContainerVisual) {
      cardContainerVisual.style.opacity = '0.3';
      cardContainerVisual.style.pointerEvents = 'none';
    }
    if (upiId) upiId.required = true;
  } else if (method === 'pod') {
    if (podFields) podFields.style.display = 'block';
    if (cardContainerVisual) {
      cardContainerVisual.style.opacity = '0.3';
      cardContainerVisual.style.pointerEvents = 'none';
    }
  }
};

// Geolocation reverse geocoder & Map locator
window.getCurrentLocation = function() {
  var addressInput = document.getElementById('del-address');
  if (navigator.geolocation) {
    if (addressInput) addressInput.value = "Locating your position...";
    navigator.geolocation.getCurrentPosition(function(position) {
      var lat = position.coords.latitude;
      var lon = position.coords.longitude;
      
      // Fetch human-readable address from OpenStreetMap reverse geocoding API
      fetch("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon)
        .then(function(res) { return res.json(); })
        .then(function(data) {
          if (addressInput) {
            if (data && data.display_name) {
              addressInput.value = data.display_name;
            } else {
              addressInput.value = "Coordinates: " + lat.toFixed(6) + ", " + lon.toFixed(6);
            }
          }
        })
        .catch(function(err) {
          if (addressInput) addressInput.value = lat.toFixed(6) + ", " + lon.toFixed(6);
        });
      
      // Update Google Maps embed iframe
      var mapIframe = document.getElementById('google-map-iframe');
      if (mapIframe) {
        mapIframe.src = "https://maps.google.com/maps?q=" + lat + "," + lon + "&t=&z=15&ie=UTF8&iwloc=&output=embed";
      }
    }, function(error) {
      if (addressInput) addressInput.value = "";
      alert("Location access denied or unavailable. Please enter your address manually.");
    });
  } else {
    alert("Geolocation is not supported by your browser.");
  }
};
