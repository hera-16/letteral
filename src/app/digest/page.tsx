'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Calendar, Download, TrendingUp, Target, AlertCircle, Lightbulb } from 'lucide-react';

interface ProgressDigest {
  id: number;
  digestType: 'WEEKLY' | 'MONTHLY' | 'QUARTERLY';
  periodStart: string;
  periodEnd: string;
  summary: string;
  totalPosts: number;
  completedGoals: number;
  ongoingGoals: number;
  challenges: number;
  topAchievements: string;
  topChallenges: string;
  keyLearnings: string;
  nextSteps: string;
  generatedAt: string;
}

export default function DigestPage() {
  const [digests, setDigests] = useState<ProgressDigest[]>([]);
  const [selectedDigest, setSelectedDigest] = useState<ProgressDigest | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'weekly' | 'monthly'>('weekly');

  useEffect(() => {
    fetchDigests(activeTab);
  }, [activeTab]);

  const fetchDigests = async (type: 'weekly' | 'monthly') => {
    setLoading(true);
    try {
      const digestType = type.toUpperCase();
      const response = await fetch(`/api/digests/type/${digestType}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setDigests(data);
        if (data.length > 0) {
          setSelectedDigest(data[0]);
        }
      }
    } catch (error) {
      console.error('Failed to fetch digests:', error);
    } finally {
      setLoading(false);
    }
  };

  const generateDigest = async (type: 'weekly' | 'monthly') => {
    setLoading(true);
    try {
      const response = await fetch(`/api/digests/${type}/current`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (response.ok) {
        const newDigest = await response.json();
        setDigests([newDigest, ...digests]);
        setSelectedDigest(newDigest);
      }
    } catch (error) {
      console.error('Failed to generate digest:', error);
    } finally {
      setLoading(false);
    }
  };

  const exportDigest = async (digestId: number, format: 'csv' | 'markdown') => {
    try {
      const response = await fetch(`/api/export/digest/${digestId}/${format}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `digest.${format}`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      }
    } catch (error) {
      console.error('Failed to export digest:', error);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <div className="container mx-auto p-6 max-w-7xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">進捗ダイジェスト</h1>
        <p className="text-gray-600">あなたの進捗を週次・月次でまとめて確認できます</p>
      </div>

      <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'weekly' | 'monthly')}>
        <TabsList className="mb-6">
          <TabsTrigger value="weekly">週次</TabsTrigger>
          <TabsTrigger value="monthly">月次</TabsTrigger>
        </TabsList>

        <TabsContent value="weekly" className="space-y-6">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold">週次ダイジェスト</h2>
            <Button
              onClick={() => generateDigest('weekly')}
              disabled={loading}
            >
              今週のダイジェストを生成
            </Button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* ダイジェスト一覧 */}
            <div className="lg:col-span-1">
              <Card>
                <CardHeader>
                  <CardTitle>過去のダイジェスト</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {digests.length === 0 ? (
                      <p className="text-sm text-gray-500">ダイジェストがありません</p>
                    ) : (
                      digests.map((digest) => (
                        <Button
                          key={digest.id}
                          variant={selectedDigest?.id === digest.id ? 'default' : 'outline'}
                          className="w-full justify-start"
                          onClick={() => setSelectedDigest(digest)}
                        >
                          <Calendar className="mr-2 h-4 w-4" />
                          {formatDate(digest.periodStart)}
                        </Button>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* ダイジェスト詳細 */}
            <div className="lg:col-span-2">
              {selectedDigest ? (
                <div className="space-y-4">
                  {/* サマリー */}
                  <Card>
                    <CardHeader>
                      <div className="flex justify-between items-start">
                        <div>
                          <CardTitle>
                            {formatDate(selectedDigest.periodStart)} ~ {formatDate(selectedDigest.periodEnd)}
                          </CardTitle>
                          <CardDescription>
                            生成日時: {formatDate(selectedDigest.generatedAt)}
                          </CardDescription>
                        </div>
                        <div className="flex gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => exportDigest(selectedDigest.id, 'csv')}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            CSV
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => exportDigest(selectedDigest.id, 'markdown')}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            Markdown
                          </Button>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="text-center p-4 bg-blue-50 rounded-lg">
                          <div className="text-2xl font-bold text-blue-600">
                            {selectedDigest.totalPosts}
                          </div>
                          <div className="text-sm text-gray-600">総投稿数</div>
                        </div>
                        <div className="text-center p-4 bg-green-50 rounded-lg">
                          <div className="text-2xl font-bold text-green-600">
                            {selectedDigest.completedGoals}
                          </div>
                          <div className="text-sm text-gray-600">達成した目標</div>
                        </div>
                        <div className="text-center p-4 bg-yellow-50 rounded-lg">
                          <div className="text-2xl font-bold text-yellow-600">
                            {selectedDigest.ongoingGoals}
                          </div>
                          <div className="text-sm text-gray-600">進行中</div>
                        </div>
                        <div className="text-center p-4 bg-red-50 rounded-lg">
                          <div className="text-2xl font-bold text-red-600">
                            {selectedDigest.challenges}
                          </div>
                          <div className="text-sm text-gray-600">課題</div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* 主な達成事項 */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <TrendingUp className="mr-2 h-5 w-5 text-green-600" />
                        主な達成事項
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.topAchievements}
                      </div>
                    </CardContent>
                  </Card>

                  {/* 主な課題 */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <AlertCircle className="mr-2 h-5 w-5 text-red-600" />
                        主な課題
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.topChallenges}
                      </div>
                    </CardContent>
                  </Card>

                  {/* 重要な学び */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <Lightbulb className="mr-2 h-5 w-5 text-yellow-600" />
                        重要な学び
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.keyLearnings}
                      </div>
                    </CardContent>
                  </Card>

                  {/* 次のステップ */}
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <Target className="mr-2 h-5 w-5 text-blue-600" />
                        次のステップ
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.nextSteps}
                      </div>
                    </CardContent>
                  </Card>
                </div>
              ) : (
                <Card>
                  <CardContent className="p-12 text-center">
                    <p className="text-gray-500">ダイジェストを選択してください</p>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        </TabsContent>

        <TabsContent value="monthly" className="space-y-6">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold">月次ダイジェスト</h2>
            <Button
              onClick={() => generateDigest('monthly')}
              disabled={loading}
            >
              今月のダイジェストを生成
            </Button>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* 同じレイアウトを使用 */}
            <div className="lg:col-span-1">
              <Card>
                <CardHeader>
                  <CardTitle>過去のダイジェスト</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {digests.length === 0 ? (
                      <p className="text-sm text-gray-500">ダイジェストがありません</p>
                    ) : (
                      digests.map((digest) => (
                        <Button
                          key={digest.id}
                          variant={selectedDigest?.id === digest.id ? 'default' : 'outline'}
                          className="w-full justify-start"
                          onClick={() => setSelectedDigest(digest)}
                        >
                          <Calendar className="mr-2 h-4 w-4" />
                          {formatDate(digest.periodStart)}
                        </Button>
                      ))
                    )}
                  </div>
                </CardContent>
              </Card>
            </div>

            <div className="lg:col-span-2">
              {selectedDigest ? (
                <div className="space-y-4">
                  {/* 月次も同じコンポーネントを使用 */}
                  <Card>
                    <CardHeader>
                      <div className="flex justify-between items-start">
                        <div>
                          <CardTitle>
                            {formatDate(selectedDigest.periodStart)} ~ {formatDate(selectedDigest.periodEnd)}
                          </CardTitle>
                          <CardDescription>
                            生成日時: {formatDate(selectedDigest.generatedAt)}
                          </CardDescription>
                        </div>
                        <div className="flex gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => exportDigest(selectedDigest.id, 'csv')}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            CSV
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => exportDigest(selectedDigest.id, 'markdown')}
                          >
                            <Download className="mr-2 h-4 w-4" />
                            Markdown
                          </Button>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div className="text-center p-4 bg-blue-50 rounded-lg">
                          <div className="text-2xl font-bold text-blue-600">
                            {selectedDigest.totalPosts}
                          </div>
                          <div className="text-sm text-gray-600">総投稿数</div>
                        </div>
                        <div className="text-center p-4 bg-green-50 rounded-lg">
                          <div className="text-2xl font-bold text-green-600">
                            {selectedDigest.completedGoals}
                          </div>
                          <div className="text-sm text-gray-600">達成した目標</div>
                        </div>
                        <div className="text-center p-4 bg-yellow-50 rounded-lg">
                          <div className="text-2xl font-bold text-yellow-600">
                            {selectedDigest.ongoingGoals}
                          </div>
                          <div className="text-sm text-gray-600">進行中</div>
                        </div>
                        <div className="text-center p-4 bg-red-50 rounded-lg">
                          <div className="text-2xl font-bold text-red-600">
                            {selectedDigest.challenges}
                          </div>
                          <div className="text-sm text-gray-600">課題</div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <TrendingUp className="mr-2 h-5 w-5 text-green-600" />
                        主な達成事項
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.topAchievements}
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <AlertCircle className="mr-2 h-5 w-5 text-red-600" />
                        主な課題
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.topChallenges}
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <Lightbulb className="mr-2 h-5 w-5 text-yellow-600" />
                        重要な学び
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.keyLearnings}
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center">
                        <Target className="mr-2 h-5 w-5 text-blue-600" />
                        次のステップ
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="whitespace-pre-wrap text-sm">
                        {selectedDigest.nextSteps}
                      </div>
                    </CardContent>
                  </Card>
                </div>
              ) : (
                <Card>
                  <CardContent className="p-12 text-center">
                    <p className="text-gray-500">ダイジェストを選択してください</p>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
