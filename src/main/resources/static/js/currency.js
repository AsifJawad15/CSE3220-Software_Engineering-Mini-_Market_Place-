/**
 * MiniMarketPlace – Live Currency Converter (BDT ↔ USD)
 * Uses open.er-api.com (free, no API key required).
 * Prices stored in BDT. Exchange rate cached in sessionStorage for 1 hour.
 */
(function () {
    'use strict';

    var CURRENCY_KEY = 'mmp_currency';
    var RATE_KEY = 'mmp_usd_bdt_rate';
    var RATE_TS_KEY = 'mmp_usd_bdt_ts';
    var RATE_TTL = 3600000; // 1 hour
    var FALLBACK_RATE = 110.5; // BDT per 1 USD

    var usdToBdtRate = FALLBACK_RATE;
    var currentCurrency = localStorage.getItem(CURRENCY_KEY) || 'BDT';

    /* ── Fetch exchange rate (cached) ─────────────────────────────── */
    function getExchangeRate() {
        var cached = sessionStorage.getItem(RATE_KEY);
        var ts = parseInt(sessionStorage.getItem(RATE_TS_KEY) || '0', 10);
        if (cached && (Date.now() - ts) < RATE_TTL) {
            return Promise.resolve(parseFloat(cached));
        }
        return fetch('https://open.er-api.com/v6/latest/USD')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                var rate = data.rates && data.rates.BDT ? data.rates.BDT : FALLBACK_RATE;
                sessionStorage.setItem(RATE_KEY, rate.toString());
                sessionStorage.setItem(RATE_TS_KEY, Date.now().toString());
                return rate;
            })
            .catch(function () {
                return parseFloat(cached) || FALLBACK_RATE;
            });
    }

    /* ── Format helper ────────────────────────────────────────────── */
    function formatPrice(bdtAmount, currency) {
        if (currency === 'USD') {
            return '$' + (bdtAmount / usdToBdtRate).toFixed(2);
        }
        return '৳' + bdtAmount.toFixed(2);
    }

    /* ── Update every [data-bdt] element on the page ─────────────── */
    function updateAllPrices() {
        document.querySelectorAll('[data-bdt]').forEach(function (el) {
            var bdt = parseFloat(el.getAttribute('data-bdt'));
            if (!isNaN(bdt)) {
                el.textContent = formatPrice(bdt, currentCurrency);
            }
        });

        // Keep toggle buttons in sync
        document.querySelectorAll('.currency-toggle-btn').forEach(function (btn) {
            var active = btn.getAttribute('data-currency') === currentCurrency;
            btn.className = active
                ? 'btn btn-sm btn-warning currency-toggle-btn fw-bold'
                : 'btn btn-sm btn-outline-secondary currency-toggle-btn';
        });

        // Update rate info badge if present
        var rateBadge = document.getElementById('exchange-rate-badge');
        if (rateBadge) {
            if (currentCurrency === 'USD') {
                rateBadge.textContent = '1 USD ≈ ৳' + usdToBdtRate.toFixed(2);
                rateBadge.style.display = '';
            } else {
                rateBadge.style.display = 'none';
            }
        }
    }

    /* ── Public API: change currency ──────────────────────────────── */
    window.setCurrency = function (currency) {
        currentCurrency = currency;
        localStorage.setItem(CURRENCY_KEY, currency);
        updateAllPrices();
    };

    /* ── Bootstrap: fetch rate then render ────────────────────────── */
    getExchangeRate().then(function (rate) {
        usdToBdtRate = rate;
        updateAllPrices();
    });

    /* ── Attach click handlers after DOM ready ────────────────────── */
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.currency-toggle-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                window.setCurrency(this.getAttribute('data-currency'));
            });
        });
    });
})();

