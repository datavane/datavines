

const targetMap = {
    test: '',
    prod: 'http://116.205.229.143:5600/',
}

const getProxy = (target, host) => {
    return [
        {
            context: ['/api'],
            target,
            changeOrigin: true,
            cookieDomainRewrite: host,
            // cookieDomainRewrite: 'https://****',
        },
    ]
}

module.exports = {
    targetMap,
    getProxy
}