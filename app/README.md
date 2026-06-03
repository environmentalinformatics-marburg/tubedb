# TubeDB (app)

TubeDB app

## Install Node.js

on windows

https://github.com/coreybutler/nvm-windows

```bash
# windows
nvm current
nvm install lts
# used working version
nvm install 24.16.0
nvm list
nvm use 24.16.0
```



## Install the dependencies
```bash
npm install

# windows workaround
npm.cmd install
```

### Start the app in development mode (hot-code reloading, error reporting, etc.)
```bash
quasar dev

# windows workaround
./node_modules/.bin/quasar.cmd dev
```

### Lint the files
```bash
npm run lint

# windows workaround
npm.cmd run lint
```

### Build the app for production
```bash
quasar build

# windows workaround
./node_modules/.bin/quasar.cmd build
```

### Customize the configuration
See [Configuring quasar.conf.js](https://quasar.dev/quasar-cli/quasar-conf-js).
