const webpack = require('webpack');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const ForkTsCheckerPlugin = require('fork-ts-checker-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const { resolve } = require('./utils');

const isDevelopment = process.env.NODE_ENV === 'development';
module.exports = {
    entry: {
        app: resolve('src/index.tsx'),
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                        options: {
                            cacheDirectory: true,
                        },
                    },
                    {
                        loader: 'ts-loader',
                        options: {
                            transpileOnly: true,
                        },
                    },
                ],
            },
            {
                test: /\.(jsx?)$/,
                // exclude: /node_modules/,
                use: [
                    {
                        loader: 'babel-loader',
                        options: {
                            cacheDirectory: true,
                        },
                    },
                ],
            },
            {
                test: /\.less$/,
                use: [
                    isDevelopment ? 'style-loader' : MiniCssExtractPlugin.loader,
                    'css-loader',
                    'postcss-loader',
                    {
                        loader: 'less-loader',
                        options: {
                            lessOptions: {
                                modifyVars: {
                                    // 'primary-color': '#8eabde',
                                    // 'link-color': '#8eabde',
                                    'border-radius-base': '4px',
                                    'font-size-base': '13px',
                                },
                                javascriptEnabled: true,
                            },
                        },
                    },
                ],
            },
            {
                test: /\.css$/,
                use: [isDevelopment ? 'style-loader' : MiniCssExtractPlugin.loader, 'css-loader', 'postcss-loader'],
            },
            {
                test: /\.(eot|otf|woff|woff2)$/,
                use: 'file-loader',
            },
            {
                test: /\.ttf$/,
                type: 'asset/resource',
            },
            {
                test: /\.(jpg|jpeg|png|svg)$/,
                use: [
                    {
                        loader: 'url-loader',
                        options: {
                            publicPath: '../',
                            limit: 1024 * 10,
                            name: 'images/[name].[ext]',
                        },
                    },
                ],
            },
        ],
    },
    resolve: {
        extensions: ['.ts', '.tsx', '.jsx', '.js', '.json'],
        alias: {
            '@': resolve('src'),
            src: resolve('src'),
            hooks: resolve('src/hooks'),
            common: resolve('src/common'),
            utils: resolve('src/utils'),
            view: resolve('src/view'),
            store: resolve('src/store'),
            component: resolve('src/component'),
            assets: resolve('src/assets'),
            '@Editor': resolve('Editor'),
        },
    },
    optimization: {
        splitChunks: {
            chunks: 'all',
            minSize: 20000,
            minRemainingSize: 0,
            minChunks: 1,
            maxAsyncRequests: 30,
            maxInitialRequests: 30,
            enforceSizeThreshold: 50000,
            cacheGroups: {
                defaultVendors: {
                    test: /[\\/]node_modules[\\/]/,
                    priority: -10,
                    reuseExistingChunk: true,
                },
                default: {
                    priority: -20,
                    reuseExistingChunk: true,
                },
            },
        },
    },
    plugins: [
        new webpack.ProgressPlugin(),
        new MiniCssExtractPlugin({
            ignoreOrder: true,
            filename: 'css/[name].[chunkhash:8].css',
        }),
        new ForkTsCheckerPlugin({
            typescript: {
                configOverwrite: {
                    compilerOptions: {
                        noUnusedLocals: false,
                    },
                },
            },
        }),
        new CopyPlugin({
            patterns: [
                { from: resolve('node_modules/monaco-editor'), to: 'monaco-editor' },
            ],
        }),
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
            'process.env.DV_ENV': JSON.stringify(process.env.DV_ENV),
        }),
    ].filter(Boolean),
};
