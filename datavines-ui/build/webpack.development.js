const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');
const ReactRefreshWebpackPlugin = require('@pmmmwh/react-refresh-webpack-plugin');
const { launchEditorMiddleware } = require('react-dev-inspector/plugins/webpack');
const { resolve, host } = require('./utils');

const TARGET_MAP = {
    test: 'http://116.205.229.143:5600',
    prod: 'http://116.205.229.143:5600',
};

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
                // cookieDomainRewrite: host,
                cookieDomainRewrite: 'https://c68a-183-193-189-215.jp.ngrok.io',
            },
            // {
            //     context: ['/api/**'],
            //     target: 'http://b394-14-127-81-48.jp.ngrok.io',
            //     // secure: false,
            //     changeOrigin: true,
            //     cookieDomainRewrite: host,
            //     // cookieDomainRewrite: 'https://5f87-183-193-191-233.ap.ngrok.io',
            // },
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
