import { Skeleton } from "@/components/ui/skeleton";
import { Card, CardContent, CardHeader } from "@/components/ui/card";

export function StatsSkeletonLoader() {
  return (
    <div className="space-y-8 animate-pulse">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {[...Array(4)].map((_, i) => (
          <Card key={i} className="border-border/50">
            <CardContent className="p-5">
              <div className="space-y-3">
                <Skeleton className="h-4 w-1/2 bg-muted/60" />
                <Skeleton className="h-8 w-1/3 bg-muted/60" />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card className="border-border/50">
          <CardHeader className="pb-2">
            <Skeleton className="h-5 w-1/3 bg-muted/60" />
          </CardHeader>
          <CardContent>
            <Skeleton className="h-[300px] w-full bg-muted/60" />
          </CardContent>
        </Card>
        <Card className="border-border/50">
          <CardHeader className="pb-2">
            <Skeleton className="h-5 w-1/3 bg-muted/60" />
          </CardHeader>
          <CardContent>
            <Skeleton className="h-[300px] w-full bg-muted/60" />
          </CardContent>
        </Card>
      </div>

      <Card className="border-border/50">
        <CardHeader className="pb-2">
          <Skeleton className="h-5 w-1/4 bg-muted/60" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-[300px] w-full bg-muted/60" />
        </CardContent>
      </Card>
    </div>
  );
}
