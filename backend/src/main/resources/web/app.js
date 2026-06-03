const api = window.location.origin;

// Application State
let state = {
  currentTab: 'stats',
  token: localStorage.getItem('autocare_user_token') || '',
  user: JSON.parse(localStorage.getItem('autocare_user_profile') || 'null'),
  charts: {},
  sharedCars: []
};

// DOM Helper
const $ = (id) => document.getElementById(id);

// --- TAB NAVIGATION SYSTEM ---
const navButtons = document.querySelectorAll('.nav-btn');
const tabContents = document.querySelectorAll('.tab-content');

navButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    const tabId = btn.getAttribute('data-tab');
    switchTab(tabId);
  });
});

function switchTab(tabId) {
  state.currentTab = tabId;
  
  // Update Buttons UI
  navButtons.forEach(b => {
    b.classList.toggle('active', b.getAttribute('data-tab') === tabId);
  });

  // Update Contents UI
  tabContents.forEach(c => {
    c.classList.toggle('active', c.id === `tab-${tabId}`);
  });

  // Load appropriate data
  if (tabId === 'stats') {
    loadGlobalStats();
  } else if (tabId === 'buyer') {
    loadBuyerGuideOptions();
  } else if (tabId === 'feed') {
    loadSharedFeed();
  } else if (tabId === 'garage') {
    checkGarageAuth();
  }
}

// Format Currency Utility
function formatCurrency(amount) {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(amount);
}

// Format Date Utility
function formatDate(millis) {
  return new Date(millis).toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

// Helper to check for active image url
function getCarImageUrl(url) {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  return `${api}${url.startsWith('/') ? '' : '/'}${url}`;
}

// --- TAB 1: GLOBAL STATS ---
async function loadGlobalStats() {
  try {
    const res = await fetch(`${api}/api/public/stats`);
    if (!res.ok) throw new Error('Ошибка загрузки статистики');
    const stats = await res.json();

    // Fill Metrics
    $('stat-total-cars').textContent = stats.totalCars;
    $('stat-total-expenses').textContent = formatCurrency(stats.totalExpenses);
    $('stat-avg-mileage').textContent = Math.round(stats.averageMileage).toLocaleString('ru-RU') + ' км';
    $('stat-avg-per-km').textContent = stats.averageExpensePerKm.toFixed(2) + ' ₽';

    renderGlobalCharts(stats);
  } catch (err) {
    console.error(err);
  }
}

// Destroy existing Chart.js instance to prevent overlap errors
function destroyChart(name) {
  if (state.charts[name]) {
    state.charts[name].destroy();
    delete state.charts[name];
  }
}

function renderGlobalCharts(stats) {
  const isLight = document.documentElement.classList.contains('light-theme');
  const chartFontColor = isLight ? '#4b5563' : '#9eaaa6';
  const gridColor = isLight ? 'rgba(0, 0, 0, 0.06)' : 'rgba(255, 255, 255, 0.05)';
  const labelFont = { family: 'Inter', size: 11 };

  // 1. Brands Popularity
  destroyChart('brands');
  const brandsCtx = $('chart-brands').getContext('2d');
  const brandLabels = Object.keys(stats.brandPopularity).slice(0, 10);
  const brandData = Object.values(stats.brandPopularity).slice(0, 10);

  state.charts['brands'] = new Chart(brandsCtx, {
    type: 'bar',
    data: {
      labels: brandLabels,
      datasets: [{
        label: 'Автомобили',
        data: brandData,
        backgroundColor: 'rgba(59, 130, 246, 0.7)',
        borderColor: 'rgb(59, 130, 246)',
        borderWidth: 1.5,
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: 'y',
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { grid: { color: gridColor }, ticks: { color: chartFontColor, font: labelFont } },
        y: { grid: { display: false }, ticks: { color: chartFontColor, font: labelFont } }
      }
    }
  });

  // 2. Expenses Category Breakdown
  destroyChart('categories');
  const catCtx = $('chart-categories').getContext('2d');
  const catLabels = Object.keys(stats.expensesByCategory);
  const catData = Object.values(stats.expensesByCategory);

  state.charts['categories'] = new Chart(catCtx, {
    type: 'doughnut',
    data: {
      labels: catLabels,
      datasets: [{
        data: catData,
        backgroundColor: [
          'rgba(168, 85, 247, 0.75)',
          'rgba(59, 130, 246, 0.75)',
          'rgba(16, 185, 129, 0.75)',
          'rgba(249, 115, 22, 0.75)',
          'rgba(239, 68, 68, 0.75)',
          'rgba(236, 72, 153, 0.75)'
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: { color: chartFontColor, font: labelFont }
        }
      }
    }
  });

  // 3. Fuel Distribution
  destroyChart('fuel');
  const fuelCtx = $('chart-fuel').getContext('2d');
  const fuelLabels = Object.keys(stats.fuelTypeDistribution);
  const fuelData = Object.values(stats.fuelTypeDistribution);

  state.charts['fuel'] = new Chart(fuelCtx, {
    type: 'polarArea',
    data: {
      labels: fuelLabels,
      datasets: [{
        data: fuelData,
        backgroundColor: [
          'rgba(59, 130, 246, 0.65)',
          'rgba(16, 185, 129, 0.65)',
          'rgba(249, 115, 22, 0.65)',
          'rgba(239, 68, 68, 0.65)'
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        r: {
          grid: { color: gridColor },
          angleLines: { color: gridColor },
          ticks: { backdropColor: 'transparent', color: chartFontColor, font: labelFont }
        }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: chartFontColor, font: labelFont }
        }
      }
    }
  });

  // 4. Fuel Prices
  destroyChart('prices');
  const pricesCtx = $('chart-fuel-prices').getContext('2d');
  const fuelPriceLabels = stats.fuelPrices.map(fp => fp.fuelType);
  const fuelPriceData = stats.fuelPrices.map(fp => fp.averagePrice);

  state.charts['prices'] = new Chart(pricesCtx, {
    type: 'bar',
    data: {
      labels: fuelPriceLabels,
      datasets: [{
        label: 'Средняя цена (₽)',
        data: fuelPriceData,
        backgroundColor: 'rgba(16, 185, 129, 0.7)',
        borderColor: 'rgb(16, 185, 129)',
        borderWidth: 1.5,
        borderRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: { grid: { display: false }, ticks: { color: chartFontColor, font: labelFont } },
        y: { grid: { color: gridColor }, ticks: { color: chartFontColor, font: labelFont } }
      }
    }
  });
}

// --- TAB 2: SHARED CARS FEED ---
async function loadSharedFeed() {
  const container = $('shared-cars-container');
  container.innerHTML = '<div class="loading">Загрузка автомобилей сообщества...</div>';

  try {
    const res = await fetch(`${api}/api/public/shared-cars`);
    if (!res.ok) throw new Error('Ошибка при загрузке ленты');
    state.sharedCars = await res.json();
    renderSharedFeed(state.sharedCars);
  } catch (err) {
    container.innerHTML = `<div class="error">Не удалось получить данные: ${err.message}</div>`;
  }
}

function renderSharedFeed(cars) {
  const container = $('shared-cars-container');
  if (cars.length === 0) {
    container.innerHTML = '<div class="no-data-msg">Никто пока еще не поделился статистикой своего авто. Вы можете быть первым!</div>';
    return;
  }

  container.innerHTML = cars.map(car => {
    const imagePath = getCarImageUrl(car.imageUrl);
    const imgHtml = imagePath 
      ? `<img src="${imagePath}" alt="${car.brand} ${car.model}" loading="lazy">` 
      : `<svg class="car-placeholder" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path><circle cx="7" cy="17" r="2"></circle><circle cx="15" cy="17" r="2"></circle></svg>`;
    
    const colorBadge = car.colorHex 
      ? `<span class="color-dot" style="background-color: ${car.colorHex};" title="${car.colorName || 'Цвет'}"></span>` 
      : '';

    return `
      <div class="car-card glass">
        <div class="car-img-container">
          <div class="owner-badge">Владелец: ${car.ownerName}</div>
          ${imgHtml}
          ${colorBadge}
        </div>
        <div class="car-details">
          <h4>${car.brand} ${car.model}</h4>
          <div class="car-generation">${car.generation} ${car.restyling || ''}</div>
          
          <div class="car-info-row">
            <div class="car-info-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><path d="M8 14s1.5 2 4 2 4-2 4-2"></path><line x1="9" y1="9" x2="9.01" y2="9"></line><line x1="15" y1="9" x2="15.01" y2="9"></line></svg>
              <span>${car.year} г.в.</span>
            </div>
            <div class="car-info-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
              <span>${car.mileage.toLocaleString('ru-RU')} км</span>
            </div>
          </div>

          <div class="car-info-row">
            <div class="car-info-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="9" y1="9" x2="15" y2="9"></line><line x1="9" y1="13" x2="15" y2="13"></line><line x1="9" y1="17" x2="13" y2="17"></line></svg>
              <span>Записей: ${car.recordsCount}</span>
            </div>
          </div>

          <div class="car-expenses-sum">
            <span class="car-expenses-label">Всего расходов</span>
            <span class="car-expenses-value">${formatCurrency(car.totalExpenses)}</span>
          </div>

          <button class="view-details-btn" onclick="openCarDetailsModal('${car.token}')">Смотреть статистику</button>
        </div>
      </div>
    `;
  }).join('');
}

// Search Filter in Feed
$('feed-search').addEventListener('input', (e) => {
  const query = e.target.value.toLowerCase().trim();
  if (!query) {
    renderSharedFeed(state.sharedCars);
    return;
  }
  const filtered = state.sharedCars.filter(car => 
    car.brand.toLowerCase().includes(query) ||
    car.model.toLowerCase().includes(query) ||
    car.ownerName.toLowerCase().includes(query) ||
    car.generation.toLowerCase().includes(query)
  );
  renderSharedFeed(filtered);
});

// --- TAB 3: USER GARAGE / AUTH ---

// Handle Auth Tabs Selection
$('btn-login-tab').addEventListener('click', () => toggleAuthTabs('login'));
$('btn-register-tab').addEventListener('click', () => toggleAuthTabs('register'));

function toggleAuthTabs(mode) {
  $('btn-login-tab').classList.toggle('active', mode === 'login');
  $('btn-register-tab').classList.toggle('active', mode === 'register');
  $('login-form').classList.toggle('active', mode === 'login');
  $('register-form').classList.toggle('active', mode === 'register');
  $('login-status').textContent = '';
  $('register-status').textContent = '';
}

function checkGarageAuth() {
  if (state.token) {
    $('auth-panel').style.display = 'none';
    $('garage-panel').style.display = 'flex';
    $('user-display-name').textContent = state.user.name;
    $('user-display-email').textContent = state.user.email;
    $('user-avatar').textContent = state.user.name.charAt(0).toUpperCase();
    loadUserGarage();
  } else {
    $('auth-panel').style.display = 'block';
    $('garage-panel').style.display = 'none';
  }
}

// Submit Login Form
$('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const statusEl = $('login-status');
  statusEl.textContent = 'Выполняется вход...';
  statusEl.className = 'form-status';

  try {
    const res = await fetch(`${api}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email: $('login-email').value.trim(),
        password: $('login-password').value.trim()
      })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Ошибка входа');

    // Save state
    state.token = data.token;
    state.user = data.user;
    localStorage.setItem('autocare_user_token', data.token);
    localStorage.setItem('autocare_user_profile', JSON.stringify(data.user));

    statusEl.textContent = 'Вход выполнен успешно!';
    statusEl.classList.add('success');

    setTimeout(() => {
      checkGarageAuth();
      // Clear fields
      $('login-email').value = '';
      $('login-password').value = '';
    }, 800);

  } catch (err) {
    statusEl.textContent = err.message;
    statusEl.classList.add('error');
  }
});

// Submit Register Form
$('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const statusEl = $('register-status');
  statusEl.textContent = 'Регистрация аккаунта...';
  statusEl.className = 'form-status';

  const nameVal = $('reg-name').value.trim();
  const emailVal = $('reg-email').value.trim();
  const passwordVal = $('reg-password').value.trim();

  if (passwordVal.length < 6) {
    statusEl.textContent = 'Пароль должен быть не менее 6 символов';
    statusEl.classList.add('error');
    return;
  }

  try {
    const res = await fetch(`${api}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: nameVal,
        email: emailVal,
        password: passwordVal
      })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Ошибка регистрации');

    state.token = data.token;
    state.user = data.user;
    localStorage.setItem('autocare_user_token', data.token);
    localStorage.setItem('autocare_user_profile', JSON.stringify(data.user));

    statusEl.textContent = 'Регистрация завершена!';
    statusEl.classList.add('success');

    setTimeout(() => {
      checkGarageAuth();
      // Clear fields
      $('reg-name').value = '';
      $('reg-email').value = '';
      $('reg-password').value = '';
    }, 800);

  } catch (err) {
    statusEl.textContent = err.message;
    statusEl.classList.add('error');
  }
});

// Logout Button
$('btn-logout').addEventListener('click', () => {
  state.token = '';
  state.user = null;
  localStorage.removeItem('autocare_user_token');
  localStorage.removeItem('autocare_user_profile');
  checkGarageAuth();
});

// Fetch Synced Cars and Sharing Status
async function loadUserGarage() {
  const container = $('my-cars-container');
  container.innerHTML = '<div class="loading">Загрузка ваших автомобилей...</div>';

  try {
    const res = await fetch(`${api}/api/user/shared-cars`, {
      headers: { 'Authorization': `Bearer ${state.token}` }
    });

    if (!res.ok) {
      if (res.status === 401) {
        // Token expired
        $('btn-logout').click();
        throw new Error('Сессия устарела, войдите снова');
      }
      throw new Error('Ошибка получения списка машин');
    }

    const cars = await res.json();
    $('my-cars-count').textContent = cars.length;
    renderUserCars(cars);

  } catch (err) {
    container.innerHTML = `<div class="error">${err.message}</div>`;
  }
}

function renderUserCars(cars) {
  const container = $('my-cars-container');
  if (cars.length === 0) {
    container.innerHTML = `
      <div class="no-data-msg">
        <h4>Ваш гараж пуст</h4>
        <p style="margin-top: 10px; font-size: 13px;">Убедитесь, что вы выполнили синхронизацию бэкапа в мобильном приложении Авто Блокнот под этим же аккаунтом!</p>
      </div>
    `;
    return;
  }

  container.innerHTML = cars.map(car => {
    const shareChecked = car.isShared ? 'checked' : '';
    const shareLinkDisplay = car.isShared ? 'flex' : 'none';
    const shareUrl = `${window.location.origin}/?shared=${car.token}`;

    return `
      <div class="my-car-row glass">
        <div class="my-car-meta">
          <h4>${car.brand} ${car.model}</h4>
          <p>Год выпуска: ${car.year} г.</p>
          <div class="share-link-box" id="linkbox-${car.id}" style="display: ${shareLinkDisplay};">
            <input type="text" readonly class="share-url-input" id="url-${car.id}" value="${shareUrl}">
            <button class="copy-btn" onclick="copyLinkText('${car.id}')">Копировать</button>
          </div>
        </div>
        <div class="sharing-controls">
          <span style="font-size: 13px; font-weight: 500; color: ${car.isShared ? 'var(--accent-green)' : 'var(--text-secondary)'}">
            ${car.isShared ? 'Поделился' : 'Поделиться'}
          </span>
          <label class="switch">
            <input type="checkbox" ${shareChecked} onchange="toggleCarSharing(this, '${car.id}')">
            <span class="slider"></span>
          </label>
        </div>
      </div>
    `;
  }).join('');
}

// Copy Link Helper
window.copyLinkText = (carId) => {
  const copyText = $(`url-${carId}`);
  copyText.select();
  copyText.setSelectionRange(0, 99999);
  navigator.clipboard.writeText(copyText.value);
  
  // Visual feedback
  const btn = copyText.nextElementSibling;
  const originalText = btn.textContent;
  btn.textContent = 'Скопировано!';
  setTimeout(() => {
    btn.textContent = originalText;
  }, 1500);
};

// Toggle Car Sharing API call
window.toggleCarSharing = async (checkbox, carId) => {
  const isChecked = checkbox.checked;
  const linkBox = $(`linkbox-${carId}`);
  const statusLabel = checkbox.parentElement.previousElementSibling;

  if (isChecked) {
    linkBox.style.display = 'flex';
    statusLabel.textContent = 'Поделился';
    statusLabel.style.color = 'var(--accent-green)';
  } else {
    linkBox.style.display = 'none';
    statusLabel.textContent = 'Поделиться';
    statusLabel.style.color = 'var(--text-secondary)';
  }

  try {
    const res = await fetch(`${api}/api/user/shared-cars/toggle`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${state.token}`
      },
      body: JSON.stringify({
        carId: parseInt(carId),
        shared: isChecked
      })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Ошибка шаринга');

    if (isChecked && data.token) {
      $(`url-${carId}`).value = `${window.location.origin}/?shared=${data.token}`;
    }

  } catch (err) {
    // Revert switch on error
    checkbox.checked = !isChecked;
    linkBox.style.display = !isChecked ? 'flex' : 'none';
    statusLabel.textContent = !isChecked ? 'Поделился' : 'Поделиться';
    statusLabel.style.color = !isChecked ? 'var(--accent-green)' : 'var(--text-secondary)';
    alert(err.message);
  }
};

// --- MODAL: SHARED CAR DETAIL VIEW ---
const modal = $('car-detail-modal');
const modalBody = $('modal-car-detail-body');

window.openCarDetailsModal = async (token) => {
  modalBody.innerHTML = '<div class="loading">Загрузка подробной статистики...</div>';
  modal.classList.add('open');

  try {
    const res = await fetch(`${api}/api/public/shared-cars/${token}`);
    if (!res.ok) throw new Error('Автомобиль не найден или доступ к нему закрыт');
    const details = await res.json();
    
    renderCarDetailsModal(details);
  } catch (err) {
    modalBody.innerHTML = `<div class="error" style="padding:40px; text-align:center;">${err.message}</div>`;
  }
};

// Close Modal functions
function closeModal() {
  modal.classList.remove('open');
  destroyChart('modal-expenses');
}

$('modal-close-btn').addEventListener('click', closeModal);
$('modal-overlay').addEventListener('click', closeModal);

function renderCarDetailsModal(details) {
  const car = details.car;
  const expenses = details.expenses;

  const imagePath = getCarImageUrl(car.imageUrl);
  const imgHtml = imagePath 
    ? `<img src="${imagePath}" alt="${car.brand} ${car.model}">` 
    : `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path><circle cx="7" cy="17" r="2"></circle><circle cx="15" cy="17" r="2"></circle></svg>`;

  // Create details layout HTML
  modalBody.innerHTML = `
    <div class="detail-header">
      <div class="detail-img">
        ${imgHtml}
      </div>
      <div class="detail-title-area">
        <div class="detail-brand-model">${car.brand} ${car.model}</div>
        <div class="detail-owner">Владелец: <span>${car.ownerName}</span></div>
        <div class="detail-badges">
          <span class="detail-badge">${car.year} г.в.</span>
          <span class="detail-badge">${car.mileage.toLocaleString('ru-RU')} км</span>
          <span class="detail-badge">${car.trim || 'Комплектация N/A'}</span>
          ${car.colorName ? `<span class="detail-badge" style="border-color:${car.colorHex}"><span style="display:inline-block; width:8px; height:8px; border-radius:50%; background-color:${car.colorHex}; margin-right:5px;"></span>${car.colorName}</span>` : ''}
        </div>
      </div>
    </div>

    <div class="detail-grid">
      <!-- Chart/Stats side -->
      <div>
        <div class="detail-section-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
          Распределение расходов (${formatCurrency(car.totalExpenses)})
        </div>
        <div class="chart-container" style="min-height: 240px;">
          <canvas id="chart-modal-expenses"></canvas>
        </div>
      </div>

      <!-- Maintenance Logs side -->
      <div>
        <div class="detail-section-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
          Журнал обслуживания (${expenses.length})
        </div>
        <div class="logs-list">
          ${expenses.map(exp => `
            <div class="log-item">
              <div class="log-main">
                <h5>${exp.title || 'Расход'}</h5>
                <div class="log-meta">${formatDate(exp.dateMillis)} • ${exp.mileage.toLocaleString('ru-RU')} км</div>
                ${exp.notes ? `<p class="log-notes">${exp.notes}</p>` : ''}
              </div>
              <div class="log-side">
                <div class="log-amount">${formatCurrency(exp.amount)}</div>
                <div class="log-category">${exp.category}</div>
              </div>
            </div>
          `).join('') || '<div class="no-data-msg">Журнал пуст</div>'}
        </div>
      </div>
    </div>
  `;

  // Render modal chart (Expenses by Category for this specific car)
  renderModalChart(expenses);
}

function renderModalChart(expenses) {
  destroyChart('modal-expenses');
  const canvas = $('chart-modal-expenses');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');

  // Compute category sums
  const catSums = {};
  expenses.forEach(e => {
    const cat = e.category || 'Другое';
    catSums[cat] = (catSums[cat] || 0.0) + e.amount;
  });

  const labels = Object.keys(catSums);
  const data = Object.values(catSums);

  state.charts['modal-expenses'] = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: [
          'rgba(168, 85, 247, 0.7)',
          'rgba(59, 130, 246, 0.7)',
          'rgba(16, 185, 129, 0.7)',
          'rgba(249, 115, 22, 0.7)',
          'rgba(239, 68, 68, 0.7)',
          'rgba(236, 72, 153, 0.7)'
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: document.documentElement.classList.contains('light-theme') ? '#4b5563' : '#9eaaa6',
            font: { family: 'Inter', size: 10 }
          }
        }
      }
    }
  });
}

// --- TAB 4: BUYER GUIDE (CHOOSE CAR) ---
let buyerOptionsList = [];

async function loadBuyerGuideOptions() {
  const brandSelect = $('buyer-brand-select');
  if (buyerOptionsList.length > 0) return; // already loaded

  brandSelect.innerHTML = '<option value="">Загрузка марок...</option>';

  try {
    const res = await fetch(`${api}/api/public/buyer-guide/options`);
    if (!res.ok) throw new Error('Ошибка загрузки марок');
    buyerOptionsList = await res.json();

    // Populate brands
    brandSelect.innerHTML = '<option value="">Выберите марку...</option>' + 
      buyerOptionsList.map(opt => `<option value="${opt.brand}">${opt.brand}</option>`).join('');

    // Setup events
    brandSelect.onchange = handleBuyerBrandChange;
    $('buyer-model-select').onchange = handleBuyerModelChange;

  } catch (err) {
    brandSelect.innerHTML = `<option value="">Ошибка: ${err.message}</option>`;
  }
}

function handleBuyerBrandChange(e) {
  const selectedBrand = e.target.value;
  const modelSelect = $('buyer-model-select');
  
  // Reset result
  $('buyer-result-container').style.display = 'none';
  $('buyer-empty-state').style.display = 'flex';

  if (!selectedBrand) {
    modelSelect.innerHTML = '<option value="">Сначала выберите марку...</option>';
    modelSelect.disabled = true;
    return;
  }

  const brandOpt = buyerOptionsList.find(opt => opt.brand === selectedBrand);
  if (brandOpt) {
    modelSelect.innerHTML = '<option value="">Выберите модель...</option>' + 
      brandOpt.models.map(m => `<option value="${m}">${m}</option>`).join('');
    modelSelect.disabled = false;
  }
}

async function handleBuyerModelChange(e) {
  const brand = $('buyer-brand-select').value;
  const model = e.target.value;

  if (!brand || !model) {
    $('buyer-result-container').style.display = 'none';
    $('buyer-empty-state').style.display = 'flex';
    return;
  }

  const resultContainer = $('buyer-result-container');
  const emptyState = $('buyer-empty-state');

  try {
    const res = await fetch(`${api}/api/public/buyer-guide/stats/${encodeURIComponent(brand)}/${encodeURIComponent(model)}`);
    if (!res.ok) throw new Error('Ошибка получения отчета по надежности');
    const stats = await res.json();

    renderBuyerStats(stats);
    emptyState.style.display = 'none';
    resultContainer.style.display = 'block';

  } catch (err) {
    alert(err.message);
  }
}

function renderBuyerStats(stats) {
  // 1. Reliability index text & visual rating
  const ratioPercent = stats.maintenanceRatio * 100;
  let scoreText = 'Низкий';
  let scoreClass = 'red';
  let reliabilityScore = 'Низкая (<55%)';

  if (ratioPercent <= 12) {
    reliabilityScore = 'Отличная (90%+)';
    scoreClass = 'green';
  } else if (ratioPercent <= 24) {
    reliabilityScore = 'Хорошая (75%-90%)';
    scoreClass = 'green';
  } else if (ratioPercent <= 45) {
    reliabilityScore = 'Средняя (55%-75%)';
    scoreClass = 'orange';
  }

  const iconEl = $('buyer-reliability-icon');
  iconEl.className = 'metric-icon ' + scoreClass;
  $('buyer-stat-reliability').textContent = reliabilityScore;
  $('buyer-stat-reliability-desc').textContent = `Затраты на ремонт составляют ${Math.round(ratioPercent)}% от общих`;

  // 2. Populate other metrics
  $('buyer-stat-avg-maintenance').textContent = formatCurrency(stats.averageMaintenanceCost);
  $('buyer-stat-cost-per-km').textContent = stats.averageExpensePerKm.toFixed(2) + ' ₽';
  $('buyer-stat-samples').textContent = stats.carCount + ' авто';

  // 3. Render doughnut chart for category breakdown
  destroyChart('buyer-categories');
  const ctx = $('chart-buyer-categories').getContext('2d');
  const labels = Object.keys(stats.categoryBreakdown);
  const data = Object.values(stats.categoryBreakdown);

  state.charts['buyer-categories'] = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: [
          'rgba(168, 85, 247, 0.75)',
          'rgba(59, 130, 246, 0.75)',
          'rgba(16, 185, 129, 0.75)',
          'rgba(249, 115, 22, 0.75)',
          'rgba(239, 68, 68, 0.75)',
          'rgba(236, 72, 153, 0.75)'
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: {
            color: document.documentElement.classList.contains('light-theme') ? '#4b5563' : '#9eaaa6',
            font: { family: 'Inter', size: 10 }
          }
        }
      }
    }
  });

  // 4. Render common repairs map list
  const repairsContainer = $('buyer-repairs-container');
  if (stats.commonRepairs.length === 0) {
    repairsContainer.innerHTML = '<div class="no-data-msg">Нет достаточных данных о проведенных ремонтах в базе.</div>';
    return;
  }

  repairsContainer.innerHTML = stats.commonRepairs.map(rep => {
    return `
      <div class="buyer-repair-item">
        <div class="buyer-repair-info">
          <h5>${rep.partName}</h5>
          <div class="buyer-repair-mileage">Ср. пробег замены: ${rep.averageMileage > 0 ? rep.averageMileage.toLocaleString('ru-RU') + ' км' : 'N/A'}</div>
        </div>
        <div class="buyer-repair-cost">
          <div class="buyer-repair-price">${formatCurrency(rep.averageCost)}</div>
          <div class="buyer-repair-count">Записей: ${rep.count}</div>
        </div>
      </div>
    `;
  }).join('');
}

// --- THEME SWITCHER SYSTEM ---
function initTheme() {
  const themeToggleBtn = $('theme-toggle');
  if (!themeToggleBtn) return;

  const currentTheme = localStorage.getItem('theme') || 'dark';
  if (currentTheme === 'light') {
    document.documentElement.classList.add('light-theme');
  }

  themeToggleBtn.addEventListener('click', () => {
    const isLight = document.documentElement.classList.toggle('light-theme');
    const newTheme = isLight ? 'light' : 'dark';
    localStorage.setItem('theme', newTheme);
    
    // Update charts theme instantly
    updateChartsTheme();
  });
}

function updateChartsTheme() {
  const isLight = document.documentElement.classList.contains('light-theme');
  const fontColor = isLight ? '#4b5563' : '#9eaaa6';
  const gridColor = isLight ? 'rgba(0, 0, 0, 0.06)' : 'rgba(255, 255, 255, 0.05)';

  Object.values(state.charts).forEach(chart => {
    if (!chart) return;
    
    // Update legend font color
    if (chart.options.plugins && chart.options.plugins.legend && chart.options.plugins.legend.labels) {
      chart.options.plugins.legend.labels.color = fontColor;
    }
    
    // Update scales (if applicable)
    if (chart.options.scales) {
      Object.values(chart.options.scales).forEach(scale => {
        if (scale.ticks) scale.ticks.color = fontColor;
        if (scale.grid) scale.grid.color = gridColor;
        if (scale.angleLines) scale.angleLines.color = gridColor;
        if (scale.pointLabels) scale.pointLabels.color = fontColor;
      });
    }
    
    chart.update();
  });
}

// --- INIT APP FLOW ---
window.addEventListener('DOMContentLoaded', () => {
  // Initialize Theme switcher
  initTheme();

  // Check if page opened with a direct shared car link: e.g. /?shared=abcd
  const urlParams = new URLSearchParams(window.location.search);
  const sharedToken = urlParams.get('shared');
  
  if (sharedToken) {
    switchTab('feed');
    openCarDetailsModal(sharedToken);
  } else {
    // Default Tab
    switchTab('stats');
  }
});
