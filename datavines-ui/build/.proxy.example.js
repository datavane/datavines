

const targetMap = {
    test: '',
    prod: '',
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