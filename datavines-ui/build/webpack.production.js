const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const { resolve } = require('./utils');

module.exports = {
    mode: 'production',
    performance: {
        hints: false,
        maxAssetSize: 250000,
    },
    output: {
        globalObject: 'self',
        publicPath: './',
        path: resolve('dist'),
        filename: 'js/[name].[fullhash:8].js',
        chunkFilename: 'js/[name].[chunkhash:8].js',
    },
    externals: {},
    optimization: {
        minimize: true,
        minimizer: [
            new TerserPlugin({
                extractComments: false,
                parallel: true,
                terserOptions: {
                    ecma: 5,
                    warnings: false,
                    compress: {
                        properties: false,
                        drop_console: process.env.DV_ENV === 'prod' || false,
                    },
                    output: {
                        comments: false,
                        beautify: false,
                    },
                },
            }),
        ],
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: resolve('./public/index-prod.html'),
        }),
    ],
};
