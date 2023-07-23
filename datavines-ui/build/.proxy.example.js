

const targetMap = {
    test: 'http://116.205.236.108:5600',
    prod: 'http://127.0.0.1:5600/',
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
