// jsdom does not implement ResizeObserver; React Flow requires it.
global.ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
};
