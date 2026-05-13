import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  pageSize?: number;
  onPageSizeChange?: (size: number) => void;
}

export function Pagination({ 
  currentPage, 
  totalPages, 
  onPageChange, 
  pageSize, 
  onPageSizeChange 
}: PaginationProps) {
  if (totalPages <= 1 && !onPageSizeChange) return null;

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-6 py-4 border-t border-border/50">
      <div className="flex items-center gap-2">
        {onPageSizeChange && (
          <div className="flex items-center gap-2 text-sm text-muted-foreground mr-4">
            <span>Éléments par page:</span>
            <Select 
              value={String(pageSize)} 
              onValueChange={(val) => onPageSizeChange(Number(val))}
            >
              <SelectTrigger className="w-20 h-8">
                <SelectValue placeholder={String(pageSize)} />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="10">10</SelectItem>
                <SelectItem value="20">20</SelectItem>
                <SelectItem value="50">50</SelectItem>
              </SelectContent>
            </Select>
          </div>
        )}
        <span className="text-sm text-muted-foreground">
          Page <span className="font-medium text-foreground">{currentPage + 1}</span> sur <span className="font-medium text-foreground">{totalPages || 1}</span>
        </span>
      </div>

      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="gap-1 h-8"
        >
          <ChevronLeft className="h-4 w-4" />
          Précédent
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="gap-1 h-8"
        >
          Suivant
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
