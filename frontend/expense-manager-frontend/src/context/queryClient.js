import { QueryCache, MutationCache , QueryClient } from "@tanstack/react-query";
 
export const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error) => {
      console.error(`Query error: ${error.message}`)
    }
  }),
  mutationCache: new MutationCache({
    onError: (error) => {
      console.error(`Mutation error: ${error.message}`);
    }
  }),
      defaultOptions: {
        queries: {
          staleTime: 1000 * 60 * 5,
        }
      }
    })
    