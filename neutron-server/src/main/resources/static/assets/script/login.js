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

function resetSendVerifyCodeButtonAndErrorHandler(e, err) {
  e.disabled = false
  e.innerText = '发送验证码'
  if (err) {
    alert(err.error_description ?? '请稍后重试')
  }
}