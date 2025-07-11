function resetSendVerifyCodeButtonAndErrorHandler(e, err) {
  e.disabled = false
  e.innerText = '发送验证码'
  if (err) {
    alert(err.error_description ?? '请稍后重试')
  }
}

function sendVerifyCode(e) {
  const phone = document.getElementById('phone').value;

  if (!phone) {
    alert('请输入手机号!');
    return false;
  }
  e.disabled = true

  fetch('/sendVerifyCode?type=LOGIN&phone=' + phone, {method: 'GET'})
    .then(resp => resp.json())
    .then(resp => {
      let expireIn = resp.expireIn
      if (expireIn) {
        e.innerText = expireIn + '秒'
        const interval = setInterval(() => {
          e.innerText = --expireIn + '秒'
          if (expireIn <= 0) {
            clearInterval(interval)
            resetSendVerifyCodeButtonAndErrorHandler(e)
          }
        }, 1000)
      } else {
        resetSendVerifyCodeButtonAndErrorHandler(e, resp)
      }
    })
    .catch(err => resetSendVerifyCodeButtonAndErrorHandler(e, err));
}

function togglePassword(e) {
  const closed = e.children[0].classList.contains('icon-eye-slash');
  const passwordInput = document.getElementById("password");
  if (closed) {
    e.children[0].classList.remove('icon-eye-slash');
    e.children[0].classList.add('icon-eye');
    passwordInput.type = 'text';
  } else {
    e.children[0].classList.remove('icon-eye');
    e.children[0].classList.add('icon-eye-slash');
    passwordInput.type = 'password';
  }
}

function initTab() {
  const nav = document.getElementById("nav")
  const content = document.getElementById("content")

  nav.addEventListener("click", function (e) {
    const navChildren = nav.children
    for (let i = 0; i < navChildren.length; i++) {
      navChildren[i].classList.remove("active")
    }

    const contentChildren = content.children
    for (let i = 0; i < contentChildren.length; i++) {
      contentChildren[i].classList.remove("active")
    }

    e.target.classList.add("active")
    document.getElementById(e.target.getAttribute('for')).classList.add("active")
  })
}

window.onload = function () {
  initTab()
}