<%@ page pageEncoding="UTF-8" %>
<style>
    html, body { height: 100%; }
    .sitemesh-navbar {
        background: rgba(255, 255, 255, .97) !important;
        backdrop-filter: blur(8px);
        border-bottom: 1px solid #dde3ec;
        z-index: 1030;
    }
    .sitemesh-brand-mark {
        width: 9px;
        height: 22px;
        display: inline-block;
        background: #2358c4;
        border-radius: 2px;
    }
    .sitemesh-nav-link {
        color: #4b5565 !important;
        font-weight: 600;
        font-size: 14px;
        padding: 8px 12px !important;
        border-radius: 6px;
        transition: background-color .15s ease, color .15s ease;
    }
    .sitemesh-nav-link:hover,
    .sitemesh-nav-link.active {
        color: #172033 !important;
        background: #eef3fb;
    }
    .sitemesh-avatar {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background: #172033;
        color: #ffffff;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 700;
        text-transform: uppercase;
    }
    .sitemesh-main-wrap {
        flex: 1 0 auto;
        display: flex;
        flex-direction: column;
    }
    .landing-page .home-hero,
    .auth-page,
    .error-page,
    .admin-shell {
        flex: 1 0 auto;
        min-height: 0 !important;
    }
    .home-hero {
        min-height: 0 !important;
    }
    .auth-page,
    .error-page {
        display: flex;
        flex-direction: column;
        padding: 28px 20px !important;
    }
    .auth-page .auth-card,
    .error-page .error-card {
        margin: auto;
    }
    .home-hero-inner {
        padding: 44px 0 !important;
    }
    .sitemesh-footer {
        background: #ffffff;
        border-top: 1px solid #dde3ec;
        color: #697386;
        font-size: 13px;
    }
    body.landing-page .sitemesh-footer {
        background: #10192b;
        border-top-color: #24324d;
        color: #9cb0d1;
    }
    .input-control.is-invalid,
    .textarea-control.is-invalid,
    .admin-field input.is-invalid,
    .admin-field select.is-invalid {
        border-color: #dc3545 !important;
        box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, .15) !important;
    }
    .invalid-feedback {
        font-size: 12.5px;
        margin-top: 5px;
        font-weight: 500;
    }
</style>
<script>
window.ShopValidation = {
    setError: function (input, message) {
        if (!input) return;
        input.setCustomValidity(message || '');
        if (message) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
        } else {
            input.classList.remove('is-invalid');
        }
        var parent = input.parentElement;
        var feedback = parent ? parent.querySelector('.invalid-feedback') : null;
        if (!feedback && parent) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            parent.appendChild(feedback);
        }
        if (feedback) {
            feedback.textContent = message || '';
            if (message) {
                feedback.classList.add('d-block');
            } else {
                feedback.classList.remove('d-block');
            }
        }
    },
    clearAll: function (form) {
        form.querySelectorAll('.is-invalid').forEach(function (el) {
            el.classList.remove('is-invalid');
        });
        form.querySelectorAll('.invalid-feedback').forEach(function (el) {
            el.classList.remove('d-block');
        });
    },
    attach: function (formId, validatorFn) {
        var form = document.getElementById(formId);
        if (!form) return;
        form.setAttribute('novalidate', 'novalidate');
        form.querySelectorAll('input, textarea, select').forEach(function (field) {
            field.addEventListener('input', function () {
                window.ShopValidation.setError(field, '');
            });
            field.addEventListener('change', function () {
                window.ShopValidation.setError(field, '');
            });
        });
        form.addEventListener('submit', function (event) {
            window.ShopValidation.clearAll(form);
            var isValid = validatorFn(form);
            if (!isValid) {
                event.preventDefault();
                event.stopPropagation();
                var firstInvalid = form.querySelector('.is-invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
            }
        });
    }
};
</script>
