Deployment notes for frontend

Chosen approach
- Pinned `vite` to 7.x ("^7.3.5") to match the peer dependency requirement of the installed `@vitejs/plugin-react@4.x`.
- This keeps the default `@vitejs/plugin-react` and avoids switching to new plugin packages.

Install and build (Windows, bash)

```bash
cd "D:/Downloads/Hotel_Room_Reservation_System/frontend"
# clean and install with normal resolve
rm -rf node_modules package-lock.json
npm i
# build
npm run build
```

If you run into peer dependency issues on other machines or CI, you can use a safe fallback:

```bash
npm i --legacy-peer-deps
```

Notes
- Alternative: upgrade `@vitejs/plugin-react` to a version that supports Vite 8, or migrate to `@vitejs/plugin-react-oxc` when you're ready to move to Vite 8.
- After deployment, ensure the server serving the `dist/` folder is configured correctly (static file hosting or a HTTP server).