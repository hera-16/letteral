'use client';

import React, { useState } from 'react';
import { ChevronDown, ChevronRight, Users, Building2, Folder, FolderOpen } from 'lucide-react';
import { OrganizationTreeNode as OrganizationTreeNodeType } from '../services/api';

export type { OrganizationTreeNodeType as OrganizationTreeNode };

interface OrganizationTreeProps {
  nodes: OrganizationTreeNodeType[];
  onSelectOrganization?: (organizationId: number) => void;
  selectedOrganizationId?: number | null;
}

const TreeNode: React.FC<{
  node: OrganizationTreeNodeType;
  level: number;
  onSelect?: (organizationId: number) => void;
  selectedOrganizationId?: number | null;
}> = ({ node, level, onSelect, selectedOrganizationId }) => {
  const [isExpanded, setIsExpanded] = useState(level === 0);
  const [isHovered, setIsHovered] = useState(false);

  const hasChildren = node.children && node.children.length > 0;

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsExpanded(!isExpanded);
  };

  const handleSelect = () => {
    if (onSelect) {
      onSelect(node.id);
    }
  };

  const isSelected = selectedOrganizationId === node.id;

  return (
    <div className="select-none">
      <div
        className={`
          flex items-center gap-3 px-4 py-3 rounded-lg cursor-pointer
          transition-all duration-200 ease-in-out
          ${isSelected
            ? 'bg-gradient-to-r from-blue-50 to-blue-100 border-l-4 border-blue-600 shadow-sm'
            : isHovered
            ? 'bg-gray-100 shadow-sm scale-[1.01]'
            : 'hover:bg-gray-50'
          }
        `}
        style={{ paddingLeft: `${level * 1.5 + 1}rem` }}
        onClick={handleSelect}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        {hasChildren ? (
          <button
            onClick={handleToggle}
            className="p-1.5 hover:bg-white/80 rounded-md transition-all duration-150 hover:scale-110"
            aria-label={isExpanded ? "折りたたむ" : "展開する"}
          >
            {isExpanded ? (
              <ChevronDown className="w-5 h-5 text-blue-600" />
            ) : (
              <ChevronRight className="w-5 h-5 text-gray-500" />
            )}
          </button>
        ) : (
          <div className="w-8" />
        )}

        {hasChildren ? (
          isExpanded ? (
            <FolderOpen className="w-5 h-5 text-blue-500" />
          ) : (
            <Folder className="w-5 h-5 text-gray-400" />
          )
        ) : (
          <Building2 className="w-5 h-5 text-gray-400" />
        )}

        <div className="flex-1 flex items-center justify-between min-w-0">
          <div className="flex items-center gap-2 min-w-0 flex-1">
            <span className={`font-semibold truncate ${isSelected ? 'text-blue-900' : 'text-gray-800'}`}>
              {node.name}
            </span>
            {node.organizationType && (
              <span className={`
                text-xs font-medium px-2.5 py-1 rounded-full whitespace-nowrap
                ${isSelected
                  ? 'bg-blue-200 text-blue-900'
                  : 'bg-gray-200 text-gray-800'
                }
              `}>
                {node.organizationType}
              </span>
            )}
          </div>

          <div className={`
            flex items-center gap-1.5 text-sm ml-4
            ${isSelected ? 'text-blue-800 font-semibold' : 'text-gray-700 font-medium'}
          `}>
            <Users className="w-4 h-4" />
            <span>{node.memberCount}</span>
          </div>
        </div>
      </div>

      {hasChildren && (
        <div
          className={`
            overflow-hidden transition-all duration-300 ease-in-out
            ${isExpanded ? 'max-h-[2000px] opacity-100 mt-1' : 'max-h-0 opacity-0'}
          `}
        >
          <div className="space-y-0.5">
            {node.children.map((child) => (
              <TreeNode
                key={child.id}
                node={child}
                level={level + 1}
                onSelect={onSelect}
                selectedOrganizationId={selectedOrganizationId}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

const OrganizationTree: React.FC<OrganizationTreeProps> = ({
  nodes,
  onSelectOrganization,
  selectedOrganizationId,
}) => {
  if (!nodes || nodes.length === 0) {
    return (
      <div className="bg-gray-50 rounded-lg p-8 text-center">
        <Building2 className="w-12 h-12 text-gray-400 mx-auto mb-2" />
        <p className="text-gray-600">組織がありません</p>
      </div>
    );
  }

  return (
    <div className="space-y-1">
      {nodes.map((node) => (
        <TreeNode
          key={node.id}
          node={node}
          level={0}
          onSelect={onSelectOrganization}
          selectedOrganizationId={selectedOrganizationId}
        />
      ))}
    </div>
  );
};

export default OrganizationTree;
