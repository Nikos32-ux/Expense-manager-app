import { QueryCache, MutationCache, QueryClient } from "@tanstack/react-query";
import { persistQueryClient } from "@tanstack/react-query-persist-client";
import { createSyncStoragePersister } from "@tanstack/query-sync-storage-persister";

export const queryClient = new QueryClient({

  queryCache: new QueryCache({
    onError: (error) => {
      if (import.meta.env.MODE !== 'production') {
        console.error(`FULL_ERROR:`, error)
        console.error("MESSAGE:", error?.message);
        console.error("STACK:", error?.stack);
      }

    }
  }),

  mutationCache: new MutationCache({
    onError: (error) => {
       if (import.meta.env.MODE !== 'production') {
        console.error(`FULL_ERROR:`, error)
        console.error("MESSAGE:", error?.message);
        console.error("STACK:", error?.stack);
      }
    }
  }),

  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: (failureCount, error) => {
        if(error?.response?.status && [400,401,403,404].includes(error.response.status)){
          return false;
        }
        
        return failureCount < 3;
      }
    }
  }
})

const persister = createSyncStoragePersister({
  storage: window.localStorage,
});

export const customPersistOptions = {
  persister,
  maxAge: 1000 * 60 * 60 * 24,
  dehydrateOptions: {
    shouldDehydrateQuery: (query) => {
      return query.meta?.persist === true && query.state.status === "success";
    },
  },
}

