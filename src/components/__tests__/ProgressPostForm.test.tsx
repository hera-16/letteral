import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ProgressPostForm from '../ProgressPostForm';
import { progressPostService, organizationService } from '@/services/api';

// API サービスをモック
jest.mock('@/services/api', () => ({
  progressPostService: {
    createPost: jest.fn(),
  },
  organizationService: {
    getUserAccessibleOrganizations: jest.fn(),
  },
}));

describe('ProgressPostForm', () => {
  const mockProps = {
    tenantId: 1,
    organizationId: 1,
    authorId: 1,
    onPostCreated: jest.fn(),
    onCancel: jest.fn(),
  };

  const mockOrganizations = [
    { id: 1, name: 'Engineering', level: 0, isActive: true },
    { id: 2, name: 'Backend Team', level: 1, isActive: true },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    (organizationService.getUserAccessibleOrganizations as jest.Mock).mockResolvedValue(
      mockOrganizations
    );
  });

  it('フォームが正しくレンダリングされる', async () => {
    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(screen.getByText(/新しい投稿を作成/i)).toBeInTheDocument();
    });
  });

  it('内容が空の場合、エラーメッセージが表示される', async () => {
    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    const contentTextarea = screen.getByPlaceholderText(/今日の進捗や目標を入力/i);
    fireEvent.change(contentTextarea, {
      target: { value: '   ' }, // 空白のみ
    });

    const form = contentTextarea.closest('form');
    fireEvent.submit(form!);

    await waitFor(() => {
      expect(screen.getByText(/内容を入力してください/i)).toBeInTheDocument();
    });

    expect(progressPostService.createPost).not.toHaveBeenCalled();
  });

  it('フォーム送信が成功する', async () => {
    (progressPostService.createPost as jest.Mock).mockResolvedValue({
      id: 1,
      content: 'Test post content',
    });

    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    // 内容を入力
    const contentTextarea = screen.getByPlaceholderText(/今日の進捗や目標を入力/i);
    fireEvent.change(contentTextarea, {
      target: { value: 'Test post content' },
    });

    // 送信
    const submitButton = screen.getByRole('button', { name: /投稿する/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(progressPostService.createPost).toHaveBeenCalledWith(
        expect.objectContaining({
          content: 'Test post content',
          tenantId: 1,
          organizationId: 1,
          authorId: 1,
        })
      );
    });
  });

  it('タグを追加できる', async () => {
    (progressPostService.createPost as jest.Mock).mockResolvedValue({
      id: 1,
      content: 'Test post',
    });

    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    // 内容を入力
    const contentTextarea = screen.getByPlaceholderText(/今日の進捗や目標を入力/i);
    fireEvent.change(contentTextarea, {
      target: { value: 'Test post content' },
    });

    // タグを入力
    const tagsInput = screen.getByPlaceholderText(/例: React, バックエンド, 設計/i);
    fireEvent.change(tagsInput, {
      target: { value: 'frontend, react, testing' },
    });

    // 送信
    const submitButton = screen.getByRole('button', { name: /投稿する/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(progressPostService.createPost).toHaveBeenCalledWith(
        expect.objectContaining({
          content: 'Test post content',
          tags: ['frontend', 'react', 'testing'],
        })
      );
    });
  });

  it('投稿タイプを変更できる', async () => {
    (progressPostService.createPost as jest.Mock).mockResolvedValue({
      id: 1,
      content: 'Question post',
    });

    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    // 投稿タイプを質問に変更（ボタンとして探す）
    const questionButton = screen.getByRole('button', { name: /❓/});
    fireEvent.click(questionButton);

    // 内容を入力
    const contentTextarea = screen.getByPlaceholderText(/今日の進捗や目標を入力/i);
    fireEvent.change(contentTextarea, {
      target: { value: 'Question post content' },
    });

    // 送信
    const submitButton = screen.getByRole('button', { name: /投稿する/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(progressPostService.createPost).toHaveBeenCalledWith(
        expect.objectContaining({
          postType: 'QUESTION',
          content: 'Question post content',
        })
      );
    });
  });

  it('API エラー時にエラーメッセージが表示される', async () => {
    const errorMessage = '投稿の作成に失敗しました';
    (progressPostService.createPost as jest.Mock).mockRejectedValue(
      new Error(errorMessage)
    );

    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    // 内容を入力
    const contentTextarea = screen.getByPlaceholderText(/今日の進捗や目標を入力/i);
    fireEvent.change(contentTextarea, {
      target: { value: 'Test post content' },
    });

    // 送信
    const submitButton = screen.getByRole('button', { name: /投稿する/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/投稿の作成に失敗しました/i)).toBeInTheDocument();
    });
  });

  it('キャンセルボタンが機能する', async () => {
    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    const cancelButton = screen.getByRole('button', { name: /キャンセル/i });
    fireEvent.click(cancelButton);

    expect(mockProps.onCancel).toHaveBeenCalled();
  });

  it('組織一覧の読み込みに失敗した場合、エラーが表示される', async () => {
    (organizationService.getUserAccessibleOrganizations as jest.Mock).mockRejectedValue(
      new Error('Failed to load organizations')
    );

    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(screen.getByText(/組織一覧の取得に失敗しました/i)).toBeInTheDocument();
    });
  });

  it('投稿成功後にフォームがリセットされる', async () => {
    jest.useFakeTimers();
    (progressPostService.createPost as jest.Mock).mockResolvedValue({
      id: 1,
      content: 'Test post',
    });

    render(<ProgressPostForm {...mockProps} />);

    await waitFor(() => {
      expect(organizationService.getUserAccessibleOrganizations).toHaveBeenCalled();
    });

    // 内容を入力
    const contentTextarea = screen.getByPlaceholderText(/今日の進捗や目標を入力/i);
    fireEvent.change(contentTextarea, {
      target: { value: 'Test post content' },
    });

    // 送信
    const submitButton = screen.getByRole('button', { name: /投稿する/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(progressPostService.createPost).toHaveBeenCalled();
    });

    // タイマーを進めてフォームのリセットを待つ
    jest.advanceTimersByTime(1500);

    await waitFor(() => {
      expect(mockProps.onPostCreated).toHaveBeenCalled();
    });

    jest.useRealTimers();
  });
});
