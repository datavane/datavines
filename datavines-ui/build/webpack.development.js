const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');
const { launchEditorMiddleware } = require('react-dev-inspector/plugins/webpack');
const { resolve, host } = require('./utils');

const TARGET_MAP = {
    test: '',
    prod: 'http://116.205.229.143:5600/',
};
let proxy = [];
try {
    // eslint-disable-next-line global-require
    const { targetMap, getProxy } = require('./.proxy.js');
    Object.assign(TARGET_MAP, targetMap);
    proxy = getProxy(TARGET_MAP[process.env.DV_ENV], host);
} catch (error) {
    console.log('error', error);
}

console.log(TARGET_MAP, proxy);

const target = TARGET_MAP[process.env.DV_ENV] || TARGET_MAP.test;
console.log('host', host);
module.exports = {
    mode: 'development',
    devtool: 'cheap-module-source-map',
    output: {
        globalObject: 'self',
        publicPath: '/',
        path: resolve('dist'),
        filename: 'js/[name].[fullhash:8].js',
        chunkFilename: 'js/[name].[chunkhash:8].js',
    },
    resolve: {
        // mainFiles: ['fortest'],
    },
    // externals: {},
    devServer: {
        before: (app) => {
            app.use(launchEditorMiddleware);
        },
        publicPath: '/',
        host,
        disableHostCheck: true,
        contentBase: resolve('dist'),
        compress: true,
        port: 5000,
        open: true,
        hot: true,
        // https: true,
        historyApiFallback: true,
        proxy: [
            {
                context: ['/api'],
                target,
                changeOrigin: true,
                cookieDomainRewrite: host,
            },
            ...proxy,
        ],
    },
    plugins: [
        new webpack.HotModuleReplacementPlugin(),
        new ReactRefreshWebpackPlugin(),
        new HtmlWebpackPlugin({
            template: resolve('./public/index-dev.html'),
        }),
    ],
};
